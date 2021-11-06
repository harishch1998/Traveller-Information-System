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
    @Column(name = "country")
    private String country;
    @Column(name = "currency")
    private String currency;
    @Column(name = "vaccinations")
    private String vaccinations;
    @Column(name = "advises")
    private String advise;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAdvise() {
        return advise;
    }

    public void setAdvise(String advise) {
        this.advise = advise;
    }

    public String getVaccinations() {
        return vaccinations;
    }

    public void setVaccinations(String vaccinations) {
        this.vaccinations = vaccinations;
    }

    public Topic(){}
//
//    public Topic(Long id, String label) {
//        this.id = id;
//        this.label = label;
//    }

    public Topic(Long id, String country, String currency, String advise, String vaccinations) {
        this.id = id;
        this.country = country;
        this.currency = currency;
        this.advise = advise;
        this.vaccinations = vaccinations;
    }
}
