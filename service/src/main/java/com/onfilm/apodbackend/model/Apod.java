package com.onfilm.apodbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Represents an Astronomy Picture of the Day (APOD) entry.
 * This entity stores details about a specific APOD, including its date, title,
 * explanation, and various media-related URLs and credits.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Apod {
    @Id
    private LocalDate date;
    @Column(columnDefinition = "TEXT")
    private String title;
    @Column(columnDefinition = "TEXT")
    private String credit;
    @Column(columnDefinition = "TEXT")
    private String explanation;
    private String hdurl;
    @JsonProperty("service_version")
    private String serviceVersion;
    @Column(columnDefinition = "TEXT")
    private String copyright;
    @JsonProperty("media_type")
    private String mediaType;
    private String url;
}