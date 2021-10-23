package com.travellerAPI.Traveller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class traveller {
	public static void main(String[] args) {
		SpringApplication.run(traveller.class, args);
	}

}
