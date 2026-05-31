package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.model.Apod;
import com.onfilm.apodbackend.repository.ApodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ApodServiceImpl} class.
 */
@ExtendWith(MockitoExtension.class)
class ApodServiceImplTest {

    @Mock
    private NasaClient nasaClient;

    @Mock
    private ApodRepository apodRepository;

    @InjectMocks
    private ApodServiceImpl apodService;

    /**
     * Tests that {@code getApod} successfully calls {@code NasaClient} and returns the expected response.
     */
    @Test
    void getApod_ShouldCallNasaClientAndReturnResponse() {
        LocalDate date = LocalDate.of(2023, 10, 1);
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("Test Title");

        when(nasaClient.fetchApod(date)).thenReturn(Mono.just(mockResponse));

        Mono<ApodResponse> result = apodService.getApod(date);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Test Title"))
                .verifyComplete();

        verify(nasaClient).fetchApod(date);
    }

    /**
     * Tests that {@code getApod} returns an error when {@code NasaClient} throws an exception.
     */
    @Test
    void getApod_NasaClientThrowsError_ShouldReturnError() {
        LocalDate date = LocalDate.of(2023, 10, 1);
        RuntimeException expectedError = new RuntimeException("NasaClient error");

        when(nasaClient.fetchApod(date)).thenReturn(Mono.error(expectedError));

        Mono<ApodResponse> result = apodService.getApod(date);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("NasaClient error"))
                .verify();

        verify(nasaClient).fetchApod(date);
    }

    /**
     * Tests that {@code getApods} returns all APODs unsorted when no sorting or pagination parameters are provided.
     */
    @Test
    void getApods_NoParams_ShouldReturnAllApodsUnsorted() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, null, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} returns all APODs unsorted when an empty sort field is provided.
     */
    @Test
    void getApods_WithEmptySortField_ShouldReturnAllApodsUnsorted() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods("", "asc", null, null); // Empty sortField

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} returns APODs sorted in ascending order when specified.
     */
    @Test
    void getApods_WithSortAsc_ShouldReturnSortedApods() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        Sort sort = Sort.by(Sort.Direction.ASC, "date");
        when(apodRepository.findAll(sort)).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods("date", "asc", null, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(sort);
    }

    /**
     * Tests that {@code getApods} returns APODs sorted in descending order when specified.
     */
    @Test
    void getApods_WithSortDesc_ShouldReturnSortedApods() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod2, apod1); // Expecting descending order

        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        when(apodRepository.findAll(sort)).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods("date", "desc", null, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .verifyComplete();

        verify(apodRepository).findAll(sort);
    }

    /**
     * Tests that {@code getApods} returns an empty Flux when the repository is empty.
     */
    @Test
    void getApods_EmptyRepository_ShouldReturnEmptyFlux() {
        when(apodRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        Flux<ApodResponse> result = apodService.getApods(null, null, null, null);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} with offset parameter skips the specified number of elements.
     */
    @Test
    void getApods_WithOffset_ShouldSkipElements() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        Apod apod3 = new Apod();
        apod3.setDate(LocalDate.of(2023, 1, 3));
        apod3.setTitle("Apod 3");
        List<Apod> mockApods = Arrays.asList(apod1, apod2, apod3);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, 1, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 3"))
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} with size parameter limits the number of elements returned.
     */
    @Test
    void getApods_WithSize_ShouldLimitElements() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        Apod apod3 = new Apod();
        apod3.setDate(LocalDate.of(2023, 1, 3));
        apod3.setTitle("Apod 3");
        List<Apod> mockApods = Arrays.asList(apod1, apod2, apod3);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, null, 2);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} with offset and size parameters correctly paginates the results.
     */
    @Test
    void getApods_WithOffsetAndSize_ShouldPaginateElements() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        Apod apod3 = new Apod();
        apod3.setDate(LocalDate.of(2023, 1, 3));
        apod3.setTitle("Apod 3");
        List<Apod> mockApods = Arrays.asList(apod1, apod2, apod3);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, 1, 1);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(Sort.unsorted());
    }

    /**
     * Tests that {@code getApods} with sort, order, offset, and size parameters correctly sorts, paginates, and limits the results.
     */
    @Test
    void getApods_WithSortOrderOffsetAndSize_ShouldReturnSortedPaginatedAndLimitedApods() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        Apod apod3 = new Apod();
        apod3.setDate(LocalDate.of(2023, 1, 3));
        apod3.setTitle("Apod 3");
        List<Apod> mockApods = Arrays.asList(apod3, apod2, apod1); // Sorted descending by date

        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        when(apodRepository.findAll(sort)).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods("date", "desc", 1, 1);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
                .verifyComplete();

        verify(apodRepository).findAll(sort);
    }
}