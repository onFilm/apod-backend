package com.onfilm.apodbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

/**
 * Configuration class for NASA API properties.
 * This class loads the NASA API base URL from the application properties.
 */
@Configuration
@Getter
public class NasaApiConfig {

    @Value("${nasa.api.base-url}")
    private String baseUrl;
}
