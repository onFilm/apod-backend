package com.onfilm.apodbackend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {"nasa.api.base-url=http://test-nasa-api.com"})
class NasaApiConfigTest {

    @Autowired
    private NasaApiConfig nasaApiConfig;

    @Test
    void baseUrl_ShouldBeLoadedFromProperties() {
        assertNotNull(nasaApiConfig);
        assertEquals("http://test-nasa-api.com", nasaApiConfig.getBaseUrl());
    }
}
