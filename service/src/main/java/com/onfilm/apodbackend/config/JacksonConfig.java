package com.onfilm.apodbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures Jackson's {@link ObjectMapper} to support Java 8 Date and Time API types.
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates a primary {@link ObjectMapper} bean that is customized to include the {@link JavaTimeModule}.
     * This module enables the proper serialization and deserialization of {@code java.time} objects,
     * such as {@code LocalDate}, {@code LocalDateTime}, and {@code ZonedDateTime}.
     *
     * @return A customized {@link ObjectMapper} with the {@link JavaTimeModule} registered.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}