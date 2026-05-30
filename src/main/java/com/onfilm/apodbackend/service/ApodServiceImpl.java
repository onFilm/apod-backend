package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Service implementation for handling APOD business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApodServiceImpl implements ApodService {

    private final NasaClient nasaClient;

    /**
     * Fetches APOD data from the NASA client.
     *
     * @param date The date to fetch data for.
     * @return A Mono emitting the ApodResponse.
     */
    @Override
    public Mono<ApodResponse> getApod(LocalDate date) {
        log.debug("Delegating fetch for date {} to NasaClient", date);
        return nasaClient.fetchApod(date);
    }
}
