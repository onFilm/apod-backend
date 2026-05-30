package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.config.NasaApiConfig;
import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.exception.NasaApiCallException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class NasaClient {

    private final WebClient webClient;
    private final NasaApiConfig nasaApiConfig;

    public Mono<ApodResponse> fetchApod(LocalDate date) {
        return webClient.get()
                .uri(nasaApiConfig.getBaseUrl(), uriBuilder -> uriBuilder
                        .queryParam("api_key", nasaApiConfig.getApiKey())
                        .queryParam("date", date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .build())
                .retrieve()
                .bodyToMono(ApodResponse.class)
                .onErrorMap(WebClientException.class, ex ->
                        new NasaApiCallException("Failed to fetch APOD data from NASA API: " + ex.getMessage(), ex));
    }
}
