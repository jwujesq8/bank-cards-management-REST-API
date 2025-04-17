package com.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Class BankCardsManagementRestApiApplication
 *
 * Main application class for the Bank Cards Management REST API.
 * This class serves as the entry point for the Spring Boot application and enables scheduling functionality.
 *
 * It contains the main method to start the application.
 *
 * The @SpringBootApplication annotation triggers the auto-configuration of the Spring application context, component scanning,
 * and other configurations necessary to run the Spring Boot application.
 *
 * The @EnableScheduling annotation enables support for scheduling tasks within the application.
 *
 * The application is designed for managing bank cards and performing various operations like transactions and card management.
 */
@EnableScheduling
@SpringBootApplication
public class BankCardsManagementRestApiApplication {


	/**
	 * The main method, serving as the entry point for the Spring Boot application.
	 * It launches the application by calling SpringApplication.run().
	 *
	 * @param args Command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		SpringApplication.run(BankCardsManagementRestApiApplication.class, args);
	}

}
