package com.travellerAPI.Traveller.Controllers;

import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.Topic;
import com.travellerAPI.Traveller.Models.User;
import com.travellerAPI.Traveller.Repositories.IUserDataRepository;
import com.travellerAPI.Traveller.Services.IUserDataService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

@RestController
@CrossOrigin
public class UserDataController {

    private static final Logger logger = LoggerFactory.getLogger(UserDataController.class);

    @Autowired
    private IUserDataService userDataService;
    @Autowired
    private IUserDataRepository userDataRepository;

    @PostMapping(
            path = "/saveLabel",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public void addLabel(String label) {
        logger.info("START UserDataController addLabel()");
        Topic topic = new Topic((long)0, label);
        Topic savedTopic = userDataRepository.saveLabel(topic);
        if(savedTopic != null){
            logger.info("Saved topic successfully!"+savedTopic.getLabel());
        }
        logger.info("END UserDataController addLabel()");
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
    public List<Map<String, String>> sendNotifications(String event) {
        logger.info("START UserDataController notify()");
        switch (event) {
            case "india" :
                updateIndia();
                break;
            case "egypt" :
                updateEgypt();
                break;
            case "singapore" :
                updateSingapore();
                break;
        }
        //This function is called repeatedly from the frontend using long polling and it checks pending notifications in the database
        //Check the notification table for notification with status as PENDING and get the topic and update status to COMPLETED
        String topicPending = userDataService.checkPendingNotification();
        //Get all the subscribers for this topic
        List<User> subscribersForTopic = userDataService.getUsersBySubscription(topicPending);
        //Get entire topic data for this topic
        Map<String, String> topicData = userDataService.getTopicData(topicPending);
        if(topicData == null)
            return null;
        List<Map<String, String>> result = new ArrayList<>();
        //returns the topic data related to those pending notifications
        for(User user : subscribersForTopic) {
            Map<String, String> map = new HashMap<>();
            map.put("user_id", String.valueOf(user.getId()));
            map.put("user_name", user.getName());
            map.put("topic_name", topicPending);
            map.put("currency", topicData.get("currency"));
            map.put("vaccinations", topicData.get("vaccinations"));
            map.put("advise", topicData.get("advise"));
            result.add(map);
        }
        logger.info("END UserDataController notify()");
        return result;
    }

    private void updateIndia() {
        logger.info("START UserDataController updateIndia()");
        userDataService.getIndiaEventData();
        logger.info("END UserDataController updateIndia()");
    }

    private void updateEgypt() {
        logger.info("START UserDataController updateEgypt()");
        userDataService.getEgyptEventData();
        logger.info("END UserDataController updateEgypt()");
    }

    private void updateSingapore() {
        logger.info("START UserDataController updateSingapore()");
        userDataService.getSingaporeEventData();
        logger.info("END UserDataController updateSingapore()");
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
