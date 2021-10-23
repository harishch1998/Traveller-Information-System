package com.travellerAPI.Traveller.Models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
@Table(name = "user_table")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "user_id")
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }

    @Column(name = "user_name")
    private String name;

    public User(){}
    public User(Long id, String name,Timestamp createdOn, Timestamp updatedOn) {
        this.id = id;
        this.name = name;
    }
}
