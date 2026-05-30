package com.onfilm.apodbackend.controller;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.service.ApodService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * REST controller for exposing APOD API endpoints.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ApodController {

    private final ApodService apodService;

    /**
     * Retrieves the APOD data for a specific date.
     *
     * @param date The date for which to fetch the APOD.
     * @return A Mono emitting the ApodResponse.
     */
    @GetMapping("/apod")
    public Mono<ApodResponse> getApod(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate requestDate = (date == null) ? LocalDate.now() : date;
        log.info("Received request to fetch APOD for date: {}", requestDate);
        return apodService.getApod(requestDate)
                .doOnSuccess(response -> log.info("Successfully fetched APOD for date: {}", requestDate))
                .doOnError(error -> log.error("Failed to fetch APOD for date: {}", requestDate, error));
    }

    /**
     * Retrieves a list of APOD data with optional sorting.
     *
     * @param sort The field to sort by (e.g., "date").
     * @param order The sort order (e.g., "asc", "desc").
     * @return A Flux emitting a list of ApodResponse.
     */
    @GetMapping("/apods")
    public Flux<ApodResponse> getApods(
            @RequestParam(value = "_sort", required = false) String sort,
            @RequestParam(value = "_order", required = false) String order) {
        log.info("Received request to fetch APODs with sort: {} and order: {}", sort, order);
        return apodService.getApods(sort, order)
                .doOnComplete(() -> log.info("Successfully fetched APODs with sort: {} and order: {}", sort, order))
                .doOnError(error -> log.error("Failed to fetch APODs with sort: {} and order: {}", sort, order, error));
    }
}