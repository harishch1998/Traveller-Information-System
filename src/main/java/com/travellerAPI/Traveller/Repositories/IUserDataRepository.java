package com.travellerAPI.Traveller.Repositories;

import com.travellerAPI.Traveller.Models.Notification;
import com.travellerAPI.Traveller.Models.Subscription;
import com.travellerAPI.Traveller.Models.Topic;
import com.travellerAPI.Traveller.Models.User;

import java.util.List;
import java.util.Map;

public interface IUserDataRepository {
    User saveUser(User user);
    Subscription saveSubscription(Subscription subscription);

    List<User> getAllUsers();

    boolean saveLabel(String label);

    User getUserByName(String name);

    List<Subscription> getAllSubscriptionsByTopic(String topicName);

    boolean updateSubscription(long userId, String newTopic);

    boolean updateTopicDetails(String topic, Map<String, String> topicData);

    boolean deleteSubscription(long userId);

    Notification addNotification(Notification notification);

    Topic getTopicDataForLabel(String label);

    Notification checkPendingNotification();

    boolean updateNotificationStatus(long id);

    User getUserById(long userId);

    List<Subscription> getAllSubscribers();
}
