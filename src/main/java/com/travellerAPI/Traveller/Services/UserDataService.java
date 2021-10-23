package com.travellerAPI.Traveller.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travellerAPI.Traveller.Controllers.UserDataController;
import com.travellerAPI.Traveller.Models.Notification;
import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.Topic;
import com.travellerAPI.Traveller.Models.User;
import com.travellerAPI.Traveller.Repositories.IUserDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDataService implements IUserDataService {

    private IUserDataRepository userDataRepository;
    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);
    private RestTemplate restTemplate = new RestTemplate();

    UserDataService(final IUserDataRepository userDataRepository) {
        this.userDataRepository = userDataRepository;
    }
    @Override
    public User registerUser(String name) {
        User user = new User((long)0, name, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
        User savedUser = userDataRepository.saveUser(user);
        return savedUser;
    }

    @Override
    public boolean addSubscription(long user_id, String topic) {
        Subscription subscription = new Subscription((long)0, user_id, topic);
        Subscription savedSubscription = userDataRepository.saveSubscription(subscription);
        return false;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = userDataRepository.getAllUsers();
        return userList;
    }

    @Override
    public List<Subscription> getAllSubscribers() {
        List<Subscription> subs = userDataRepository.getAllSubscribers();
        return subs;
    }

    @Override
    public User getUserByName(String name) {
        User user = userDataRepository.getUserByName(name);
        return user;
    }

    @Override
    public boolean updateSubscription(long userId, String newTopic) {
        boolean updated = userDataRepository.updateSubscription(userId, newTopic);
        return updated;
    }

    @Override
    public boolean deleteSubscription(long userId) {
        boolean deleted = userDataRepository.deleteSubscription(userId);
        return deleted;
    }

    @Override
    public String checkPendingNotification() {
        Notification notification = userDataRepository.checkPendingNotification();
        boolean updated = userDataRepository.updateNotificationStatus(notification.getId());
        logger.info("Updated notification status to completed for n_id:"+notification.getId()+" with topic: "+notification.getTopicName());
        return notification.getTopicName();
    }

    @Override
    public List<User> getUsersBySubscription(String topicPending) {
        List<Subscription> subscriptions = userDataRepository.getAllSubscriptionsByTopic(topicPending);
        List<User> users = new ArrayList<>();
        for(Subscription subscription : subscriptions) {
            User user = userDataRepository.getUserById(subscription.getUserId());
            if(user != null)
                users.add(user);
        }
        return users;
    }

    @Override
    public Map<String, String> getTopicData(String topicPending) {
        Topic topicCurrency = userDataRepository.getTopicDataForLabel("currency");
        Topic topicVaccinations = userDataRepository.getTopicDataForLabel("vaccinations");
        Topic topicAdvise = userDataRepository.getTopicDataForLabel("advise");
        Map<String, String> map = new HashMap<>();
        if(topicCurrency == null || topicVaccinations == null || topicAdvise == null)
            return null;
        switch(topicPending){
            case "india":
                map.put("currency", topicCurrency.getIndia());
                map.put("vaccinations", topicVaccinations.getIndia());
                map.put("advise", topicAdvise.getIndia());
                break;
            case "egypt":
                map.put("currency", topicCurrency.getEgypt());
                map.put("vaccinations", topicVaccinations.getEgypt());
                map.put("advise", topicAdvise.getEgypt());
                break;
            case "singapore":
                map.put("currency", topicCurrency.getSingapore());
                map.put("vaccinations", topicVaccinations.getSingapore());
                map.put("advise", topicAdvise.getSingapore());
                break;
        }
        return map;
    }

    //@Scheduled(fixedRate = 2000)
    public void sayHello() {
        logger.info("Hello from our simple scheduled method");
    }

    @Override
    public void getIndiaEventData() {
        logger.info("Hello from our simple scheduled method for India data");
        String urlIndia = "https://travelbriefing.org/India?format=json";
        String indiaDataJsonString = restTemplate.getForObject(urlIndia, String.class);
        //get event data from external API and publish event
        publish(indiaDataJsonString, "india");
    }

    @Override
    public void getEgyptEventData() {
        String urlEgypt = "https://travelbriefing.org/Egypt?format=json";
        String egyptDataJsonString = restTemplate.getForObject(urlEgypt, String.class);
        publish(egyptDataJsonString, "egypt");
    }

    //@Scheduled(fixedRate = 50000)
    @Override
    public void getSingaporeEventData() {
        String urlSingapore = "https://travelbriefing.org/Singapore?format=json";
        String singaporeDataJsonString = restTemplate.getForObject(urlSingapore, String.class);
        publish(singaporeDataJsonString, "singapore");
    }


    public void publish(String jsonData, String topic) {
        //This method populates the database with the newly fetched data and marks status to pending for the notified column
        //topics are : INDIA, EGYPT, SINGAPORE
        //we filter out currency, vaccinations and advise for these topics.
        //convert these String json to Map<String, Object> using object mapper
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> dataMap = mapper.readValue(jsonData, Map.class);
            //get to USD currency data
            Map<String, Object> currencyObj = (Map<String, Object>) dataMap.get("currency");
            String currency = String.valueOf(currencyObj.get("rate"));
            //get vaccinations data
            StringBuilder finalVaccinations = new StringBuilder();
            List<Map<String, Object>> vaccinations = (List<Map<String, Object>>) dataMap.get("vaccinations");
            for(Map<String, Object> v : vaccinations) {
                String name = String.valueOf(v.get("name"));
                finalVaccinations.append(name+", ");
            }
            finalVaccinations.deleteCharAt(finalVaccinations.length()-1);
            //get advise
            Map<String, Object> adviseObj = (Map<String, Object>) dataMap.get("advise");
            Map<String, Object> UAObj = (Map<String, Object>) adviseObj.get("UA");
            Map<String, Object> CAObj = (Map<String, Object>) adviseObj.get("CA");
            Object uaAdv = UAObj.get("advise");
            Object caAdv = CAObj.get("advise");
            String adviseFinal = String.valueOf(uaAdv) + ". " + String.valueOf(caAdv);

            Map<String, String> topicData = new HashMap<>();
            topicData.put("currency", currency);
            topicData.put("vaccinations", String.valueOf(finalVaccinations));
            topicData.put("advise", adviseFinal);
            //update all these details for topic = country in the database
            userDataRepository.updateTopicDetails(topic, topicData);
            //add notification with pending status for topic
            Notification n = userDataRepository.addNotification(new Notification((long)0, topic, "PENDING"));
            logger.info("Added notification with status PENDING For topic:"+topic);
        } catch (IOException e) {
            logger.error("Error: ", e.getMessage());
        }
    }
}
