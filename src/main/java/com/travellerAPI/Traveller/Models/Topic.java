package com.travellerAPI.Traveller.Models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "topic_details")
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "label_id")
    private long id;
    @Column(name = "label")
    private String label;
    @Column(name = "india")
    private String india;
    @Column(name = "egypt")
    private String egypt;
    @Column(name = "singapore")
    private String singapore;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getIndia() {
        return india;
    }

    public void setIndia(String india) {
        this.india = india;
    }

    public String getEgypt() {
        return egypt;
    }

    public void setEgypt(String egypt) {
        this.egypt = egypt;
    }

    public String getSingapore() {
        return singapore;
    }

    public void setSingapore(String singapore) {
        this.singapore = singapore;
    }

    public Topic(){}

    public Topic(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Topic(Long id, String label, String india, String egypt, String singapore) {
        this.id = id;
        this.label = label;
        this.india = india;
        this.egypt = egypt;
        this.singapore = singapore;
    }
}
