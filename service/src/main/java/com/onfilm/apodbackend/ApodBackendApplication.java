package com.onfilm.apodbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the APOD Backend application.
 * This class uses Spring Boot to simplify the development of production-ready Spring applications.
 */
@SpringBootApplication
public class ApodBackendApplication {

    /**
     * Main method to run the Spring Boot application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(ApodBackendApplication.class, args);
    }

}