package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.model.Apod;
import com.onfilm.apodbackend.repository.ApodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
    private final ApodRepository apodRepository;

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

    /**
     * Retrieves a list of APOD data with optional sorting and pagination.
     *
     * @param sortField The field to sort by (e.g., "date").
     * @param sortOrder The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    @Override
    public Flux<ApodResponse> getApods(String sortField, String sortOrder, Integer offset, Integer size) {
        Sort sort = Sort.unsorted();
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortField);
        }

        Flux<Apod> apodsFlux = Flux.fromIterable(apodRepository.findAll(sort));

        if (offset != null && offset >= 0) {
            apodsFlux = apodsFlux.skip(offset);
        }
        if (size != null && size > 0) {
            apodsFlux = apodsFlux.take(size);
        }

        return apodsFlux.map(this::convertToApodResponse);
    }

    /**
     * Searches for APODs based on a search term and returns a paginated list.
     *
     * @param searchTerm The term to search for in APOD titles or explanations.
     * @param sortField The field to sort by (e.g., "date").
     * @param sortOrder The sort order (e.g., "asc", "desc").
     * @param offset The starting index for pagination.
     * @param size The number of results to return for pagination.
     * @return A Flux emitting a list of ApodResponse.
     */
    @Override
    public Flux<ApodResponse> searchApods(String searchTerm, String sortField, String sortOrder, Integer offset, Integer size) {
        Sort sort = Sort.unsorted();
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortField);
        }

        int effectiveSize = (size != null && size > 0) ? size : 20; // Default size from controller
        int effectiveOffset = (offset != null && offset >= 0) ? offset : 0;
        int page = effectiveOffset / effectiveSize; // Calculate page number

        Pageable pageable = PageRequest.of(page, effectiveSize, sort);

        return Flux.fromIterable(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(searchTerm, searchTerm, pageable))
                .map(this::convertToApodResponse);
    }

    /**
     * Converts an Apod entity to an ApodResponse DTO.
     *
     * @param apod The Apod entity to convert.
     * @return The converted ApodResponse DTO.
     */
    private ApodResponse convertToApodResponse(Apod apod) {
        ApodResponse response = new ApodResponse();
        response.setDate(apod.getDate());
        response.setTitle(apod.getTitle());
        response.setCredit(apod.getCredit());
        response.setExplanation(apod.getExplanation());
        response.setHdurl(apod.getHdurl());
        response.setServiceVersion(apod.getServiceVersion());
        response.setCopyright(apod.getCopyright());
        response.setMediaType(apod.getMediaType());
        response.setUrl(apod.getUrl());
        return response;
    }
}