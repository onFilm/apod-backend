package com.onfilm.apodbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing the response from the NASA APOD (Astronomy Picture of the Day) API.
 * This class encapsulates the details of the APOD, including its title, explanation, date, and various media URLs.
 */
@Data
public class ApodResponse {

    private String title;
    private String explanation;
    private LocalDate date;
    private String url;
    @JsonProperty("media_type")
    private String mediaType;
    @JsonProperty("service_version")
    private String serviceVersion;
    @JsonProperty("hdurl")
    private String hdUrl;
    private String copyright;
}