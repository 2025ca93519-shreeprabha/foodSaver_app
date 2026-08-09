package com.agile.processes.foodSaverApp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FoodSaverAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(FoodSaverAppApplication.class, args);
	}

}
