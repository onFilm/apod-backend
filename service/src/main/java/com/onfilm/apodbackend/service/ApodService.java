package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Service interface for retrieving Astronomy Picture of the Day (APOD) data.
 */
public interface ApodService {
    /**
     * Retrieves the Astronomy Picture of the Day (APOD) for a given date.
     *
     * @param date The date for which to retrieve the APOD.
     * @return A Mono emitting the ApodResponse containing the APOD data.
     */
    Mono<ApodResponse> getApod(LocalDate date);

    /**
     * Retrieves a list of APOD data with optional sorting and pagination.
     *
     * @param sort The field to sort by (e.g., "date").
     * @param order The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    Flux<ApodResponse> getApods(String sort, String order, Integer offset, Integer size);

    /**
     * Searches for APODs based on a search term and returns a paginated list.
     *
     * @param searchTerm The term to search for in APOD titles or explanations.
     * @param sort The field to sort by (e.g., "date").
     * @param order The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    Flux<ApodResponse> searchApods(String searchTerm, String sort, String order, Integer offset, Integer size);
}