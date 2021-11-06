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
        Topic topicsIndia = userDataRepository.getTopicDataForCountry("india");
        Topic topicsEgypt = userDataRepository.getTopicDataForCountry("egypt");
        Topic topicsSingapore = userDataRepository.getTopicDataForCountry("singapore");
        Map<String, String> map = new HashMap<>();
        if(topicsIndia == null || topicsEgypt == null || topicsSingapore == null)
            return null;
        switch(topicPending){
            case "currency":
                map.put("india-currency", topicsIndia.getCurrency());
                map.put("egypt-currency", topicsEgypt.getCurrency());
                map.put("singapore-currency", topicsSingapore.getCurrency());
                break;
            case "advise":
                map.put("india-advise", topicsIndia.getAdvise());
                map.put("egypt-advise", topicsEgypt.getAdvise());
                map.put("singapore-advise", topicsSingapore.getAdvise());
                break;
            case "vaccinations":
                map.put("india-vaccinations", topicsIndia.getVaccinations());
                map.put("egypt-vaccinations", topicsEgypt.getVaccinations());
                map.put("singapore-vaccinations", topicsSingapore.getVaccinations());
                break;
        }
        return map;
    }

    @Override
    public void getAllEventData(String topic){
        //get event data from external API and publish event
        String indiaDataJsonString = getIndiaEventData();
        String egyptDataJsonString = getEgyptEventData();
        String singaporeDataJsonString = getSingaporeEventData();
        publish(indiaDataJsonString, egyptDataJsonString, singaporeDataJsonString, topic);
    }

    @Override
    public String getIndiaEventData() {
        String urlIndia = "https://travelbriefing.org/India?format=json";
        String indiaDataJsonString = restTemplate.getForObject(urlIndia, String.class);
        //publish(indiaDataJsonString, "india");
        return indiaDataJsonString;
    }

    @Override
    public String getEgyptEventData() {
        String urlEgypt = "https://travelbriefing.org/Egypt?format=json";
        String egyptDataJsonString = restTemplate.getForObject(urlEgypt, String.class);
        //publish(egyptDataJsonString, "egypt");
        return egyptDataJsonString;
    }
    //@Scheduled(fixedRate = 50000)
    @Override
    public String getSingaporeEventData() {
        String urlSingapore = "https://travelbriefing.org/Singapore?format=json";
        String singaporeDataJsonString = restTemplate.getForObject(urlSingapore, String.class);
        //publish(singaporeDataJsonString, "singapore");
        return singaporeDataJsonString;
    }

    public void publish(String indiaJsonData, String egyptJsonData, String singaporeJsonData, String topic) {
        //This method populates the database with the newly fetched data and marks status to pending for the notified column
        //topics are : currency, advise, vaccinations
        //we filter out india, egypt and singapore data for these topics
        //convert these String json to Map<String, Object> using object mapper
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> indiaDataMap = mapper.readValue(indiaJsonData, Map.class);
            Map<String, Object> egyptDataMap = mapper.readValue(egyptJsonData, Map.class);
            Map<String, Object> singaporeDataMap = mapper.readValue(singaporeJsonData, Map.class);

            Map<String, String> topicData = new HashMap<>();
            if(topic.equals("currency")){
                //get to USD currency data
                Map<String, Object> indiaCurrencyObj = (Map<String, Object>) indiaDataMap.get("currency");
                String indiaCurrency = String.valueOf(indiaCurrencyObj.get("rate"));

                Map<String, Object> egyptCurrencyObj = (Map<String, Object>) egyptDataMap.get("currency");
                String egyptCurrency = String.valueOf(egyptCurrencyObj.get("rate"));

                Map<String, Object> singaporeCurrencyObj = (Map<String, Object>) singaporeDataMap.get("currency");
                String singaporeCurrency = String.valueOf(singaporeCurrencyObj.get("rate"));

                topicData.put("indiaCurrency", indiaCurrency);
                topicData.put("egyptCurrency", egyptCurrency);
                topicData.put("singaporeCurrency", singaporeCurrency);
            } else if(topic.equals("advise")){
                //get advise
                Map<String, Object> indiaAdviseObj = (Map<String, Object>) indiaDataMap.get("advise");
                Map<String, Object> indiaUAObj = (Map<String, Object>) indiaAdviseObj.get("UA");
                Map<String, Object> indiaCAObj = (Map<String, Object>) indiaAdviseObj.get("CA");
                Object indiaUaAdv = indiaUAObj.get("advise");
                Object indiaCaAdv = indiaCAObj.get("advise");
                String indiaAdviseFinal = String.valueOf(indiaUaAdv) + ". " + String.valueOf(indiaCaAdv);

                Map<String, Object> egyptAdviseObj = (Map<String, Object>) egyptDataMap.get("advise");
                Map<String, Object> egyptUAObj = (Map<String, Object>) egyptAdviseObj.get("UA");
                Map<String, Object> egyptCAObj = (Map<String, Object>) egyptAdviseObj.get("CA");
                Object egyptUaAdv = egyptUAObj.get("advise");
                Object egyptCaAdv = egyptCAObj.get("advise");
                String egyptAdviseFinal = String.valueOf(egyptUaAdv) + ". " + String.valueOf(egyptCaAdv);

                Map<String, Object> singaporeAdviseObj = (Map<String, Object>) singaporeDataMap.get("advise");
                Map<String, Object> singaporeUAObj = (Map<String, Object>) singaporeAdviseObj.get("UA");
                Map<String, Object> singaporeCAObj = (Map<String, Object>) singaporeAdviseObj.get("CA");
                Object singaporeUaAdv = singaporeUAObj.get("advise");
                Object singaporeCaAdv = singaporeCAObj.get("advise");
                String singaporeAdviseFinal = String.valueOf(singaporeUaAdv) + ". " + String.valueOf(singaporeCaAdv);

                topicData.put("indiaAdviseFinal", indiaAdviseFinal);
                topicData.put("egyptAdviseFinal", egyptAdviseFinal);
                topicData.put("singaporeAdviseFinal", singaporeAdviseFinal);
            } else{
                //get vaccinations data
                StringBuilder indiaFinalVaccinations = new StringBuilder();
                List<Map<String, Object>> indiaVaccinations = (List<Map<String, Object>>) indiaDataMap.get("vaccinations");
                for(Map<String, Object> v : indiaVaccinations) {
                    String name = String.valueOf(v.get("name"));
                    indiaFinalVaccinations.append(name+", ");
                }
                indiaFinalVaccinations.deleteCharAt(indiaFinalVaccinations.length()-1);

                StringBuilder egyptFinalVaccinations = new StringBuilder();
                List<Map<String, Object>> egyptVaccinations = (List<Map<String, Object>>) egyptDataMap.get("vaccinations");
                for(Map<String, Object> v : egyptVaccinations) {
                    String name = String.valueOf(v.get("name"));
                    egyptFinalVaccinations.append(name+", ");
                }
                egyptFinalVaccinations.deleteCharAt(egyptFinalVaccinations.length()-1);

                StringBuilder singaporeFinalVaccinations = new StringBuilder();
                List<Map<String, Object>> singaporeVaccinations = (List<Map<String, Object>>) singaporeDataMap.get("vaccinations");
                for(Map<String, Object> v : singaporeVaccinations) {
                    String name = String.valueOf(v.get("name"));
                    singaporeFinalVaccinations.append(name+", ");
                }
                singaporeFinalVaccinations.deleteCharAt(singaporeFinalVaccinations.length()-1);

                topicData.put("indiaFinalVaccinations", String.valueOf(indiaFinalVaccinations));
                topicData.put("singaporeFinalVaccinations", String.valueOf(singaporeFinalVaccinations));
                topicData.put("egyptFinalVaccinations", String.valueOf(egyptFinalVaccinations));
            }
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
