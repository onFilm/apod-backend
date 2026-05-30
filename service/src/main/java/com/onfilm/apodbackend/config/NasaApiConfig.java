package com.onfilm.apodbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;

@Configuration
@Getter
public class NasaApiConfig {

    @Value("${nasa.api.key}")
    private String apiKey;

    @Value("${nasa.api.base-url}")
    private String baseUrl;
}
