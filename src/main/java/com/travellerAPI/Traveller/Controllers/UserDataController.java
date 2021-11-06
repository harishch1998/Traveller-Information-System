package com.travellerAPI.Traveller.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.Topic;
import com.travellerAPI.Traveller.Models.User;
import com.travellerAPI.Traveller.Repositories.IUserDataRepository;
import com.travellerAPI.Traveller.Services.IUserDataService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@RestController
@CrossOrigin
public class UserDataController {

    private static final Logger logger = LoggerFactory.getLogger(UserDataController.class);
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ServletWebServerApplicationContext applicationContext;
    @Autowired
    private IUserDataService userDataService;
    @Autowired
    private IUserDataRepository userDataRepository;

    @PostConstruct
    public void init() {
        String[] arr = {"india", "egypt", "singapore"};
        for(String country : arr) {
            addCountry(country);
        }
    }

    @PostMapping(
            path = "/saveCountry",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void addCountry(String country) {
        logger.info("START UserDataController addCountry()");
        if(!userDataRepository.countryExists(country)){
            boolean savedCountry = userDataRepository.saveCountry(country);
            if(savedCountry){
                logger.info("Saved country successfully!");
            }
        }
        logger.info("END UserDataController addCountry()");
    }

    @PostMapping(
            path = "/register",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public User registerUser(String name, String topic) {
        logger.info("START UserDataController registerUser()");
        User savedUser = userDataService.registerUser(name);
        if(savedUser != null) {
            logger.debug("Created user:" + savedUser.getName());
            boolean addedSubscription = userDataService.addSubscription(savedUser.getId(), topic);
            logger.debug("Added subscription:" + addedSubscription);
        }
        logger.info("END UserDataController registerUser()");
        return savedUser;
    }

    @PostMapping(
            path = "/subscribe",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public boolean subscribe(String name, String topic) {
        logger.info("START UserDataController subscribe()");
        User user = userDataService.getUserByName(name);
        if(user == null) {
            logger.info("User doesn't exist");
            return false;
        }
        boolean updatedSubscription = userDataService.updateSubscription(user.getId(), topic);
        if(!updatedSubscription) {
            boolean addedSubscription = userDataService.addSubscription(user.getId(), topic);
            logger.debug("Added subscription:" + addedSubscription);
            return addedSubscription;
        }
        logger.info("Updated subscription:" + true);
        logger.info("END UserDataController subscribe()");
        return true;
    }

    @PostMapping(path = "/unsubscribe",
                    consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public boolean unsubscribe(String name) {
        logger.info("START UserDataController unsubscribe()");
        User user = userDataService.getUserByName(name);
        if(user == null) {
            logger.info("User doesn't exist");
            return false;
        }
        boolean deletedSubscription = userDataService.deleteSubscription(user.getId());
        logger.info("END UserDataController unsubscribe()");
        return true;
    }
    @GetMapping("/users")
    public List<User> getAllUsers() {
        logger.info("START UserDataController getAllUsers()");
        List<User> userList = userDataService.getAllUsers();
        logger.info("END UserDataController getAllUsers()");
        return userList;
    }
    @GetMapping("/subscribers")
    public List<Subscription> getAllSubscribers() {
        logger.info("START UserDataController getAllSubscribers()");
        List<Subscription> subList = userDataService.getAllSubscribers();
        logger.info("END UserDataController getAllSubscribers()");
        return subList;
    }

    @GetMapping("/notify")
    public String sendNotifications(String event) throws JsonProcessingException {
        logger.info("START UserDataController notify()");
//        switch (event) {
//            case "currency" :
//                updateCurrency();
//                break;
//            case "advise" :
//                updateAdvise();
//                break;
//            case "vaccinations" :
//                updateVaccinations();
//                break;
//        }
        //This function is called repeatedly from the frontend using long polling, and it checks pending notifications in the database
        //Check the notification table for notification with status as PENDING and get the topic and update status to COMPLETED
        int port = applicationContext.getWebServer().getPort();
        logger.info("NOW AT PORT:"+port+" AND TOPIC:"+event);
        if(event.equals("currency")) {
            if (port == 8080) {
                logger.info("SENDING RESPONSE FROM:"+port);
                updateCurrency();
                return notifySubscribers();
            } else {
                String brokerUrl = "http://0.0.0.0:8080/notify?event=currency";
                return restTemplate.getForObject(brokerUrl, String.class);
            }
        } else if(event.equals("advise")){
            if(port == 8081){
                logger.info("SENDING RESPONSE FROM:"+port);
                updateAdvise();
                return notifySubscribers();
            } else {
                String brokerUrl = "http://0.0.0.0:8081/notify?event=advise";
                return restTemplate.getForObject(brokerUrl, String.class);
            }
        } else {
            if(port == 8082){
                logger.info("SENDING RESPONSE FROM:"+port);
                updateVaccinations();
                return notifySubscribers();
            } else {
                String brokerUrl = "http://0.0.0.0:8081/notify?event=vaccinations";
                return restTemplate.getForObject(brokerUrl, String.class);
            }
        }
    }

    private String notifySubscribers() throws JsonProcessingException {
        String topicPending = userDataService.checkPendingNotification();
        //Get all the subscribers for this topic
        List<User> subscribersForTopic = userDataService.getUsersBySubscription(topicPending);
        Map<String, String> topicData = userDataService.getTopicData(topicPending);
        if(topicData == null)
            return null;
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> result = new ArrayList<>();
        //returns the topic data related to those pending notifications
        for(User user : subscribersForTopic) {
            Map<String, String> map = new HashMap<>();
            map.put("user_id", String.valueOf(user.getId()));
            map.put("user_name", user.getName());
            String json = new ObjectMapper().writeValueAsString(topicData);
            map.put("topic-data", json);
            result.add(map);
        }
        return new ObjectMapper().writeValueAsString(result);
    }
    private void updateCurrency() {
        logger.info("START UserDataController updateCurrency()");
        userDataService.getAllEventData("currency");
        logger.info("END UserDataController updateCurrency()");
    }

    private void updateAdvise() {
        logger.info("START UserDataController updateAdvise()");
        userDataService.getAllEventData("advise");
        logger.info("END UserDataController updateAdvise()");
    }

    private void updateVaccinations() {
        logger.info("START UserDataController updateVaccinations()");
        userDataService.getAllEventData("vaccinations");
        logger.info("END UserDataController updateVaccinations()");
    }

    @GetMapping("/advertise")
    public String advertise() {
        logger.info("START UserDataController advertise()");
        logger.info("END UserDataController advertise()");
        return "INDIA, EGYPT, SINGAPORE";
    }

    @RequestMapping("/hello")
    public String sayHello(){
        return "Hello!";
    }

}
