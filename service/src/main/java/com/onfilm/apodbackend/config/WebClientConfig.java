package com.onfilm.apodbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient.
 * Provides a bean for WebClient to be used throughout the application.
 */
@Configuration
public class WebClientConfig {

    /**
     * Configures and provides a WebClient bean.
     *
     * @param builder The WebClient.Builder provided by Spring.
     * @return A configured WebClient instance.
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}