package com.travellerAPI.Traveller.Services;

import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.User;

import java.util.List;
import java.util.Map;

public interface IUserDataService {
    User registerUser(String name);
    boolean addSubscription(long id, String topic);

    List<User> getAllUsers();
    User getUserByName(String name);

    boolean updateSubscription(long id, String topic);
    boolean deleteSubscription(long id);

    String checkPendingNotification();

    List<User> getUsersBySubscription(String topicPending);

    Map<String, String> getTopicData(String topicPending);

    void getIndiaEventData();

    void getEgyptEventData();

    //@Scheduled(fixedRate = 50000)
    void getSingaporeEventData();

    List<Subscription> getAllSubscribers();
}
