package com.travellerAPI.Traveller.Models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "subscriber_table")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "subscriber_id")
    private long id;
    @Column(name = "user_id")
    private long userId;
    @Column(name = "topic_name")
    private String topicName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public Subscription(){}
    public Subscription(Long id, Long userId, String topicName) {
        this.id = id;
        this.userId = userId;
        this.topicName = topicName;
    }
}
