package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.model.Apod;
import com.onfilm.apodbackend.repository.ApodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
     * Tests that {@code getApods} returns APODs sorted in ascending order when sortField is provided and sortOrder is null.
     */
    @Test
    void getApods_WithSortFieldAndNullSortOrder_ShouldReturnSortedAscending() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        Sort sort = Sort.by(Sort.Direction.ASC, "date");
        when(apodRepository.findAll(sort)).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods("date", null, null, null);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
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
     * Tests that {@code getApods} with a negative offset parameter does not skip elements.
     */
    @Test
    void getApods_WithNegativeOffset_ShouldNotSkipElements() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, -1, null); // Negative offset

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Apod 1"))
                .expectNextMatches(response -> response.getTitle().equals("Apod 2"))
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
     * Tests that {@code getApods} with a non-positive size parameter does not limit elements.
     */
    @Test
    void getApods_WithNonPositiveSize_ShouldNotLimitElements() {
        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Apod 1");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Apod 2");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findAll(Sort.unsorted())).thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.getApods(null, null, null, 0); // Zero size

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

    /**
     * Tests that {@code searchApods} returns APODs matching the search term with default pagination.
     */
    @Test
    void searchApods_ShouldReturnMatchingApods() {
        String searchTerm = "galaxy";
        Integer size = 20; // Default size
        Integer offset = 0; // Default offset
        Sort sort = Sort.unsorted();
        Pageable pageable = PageRequest.of(offset / size, size, sort);

        Apod apod1 = new Apod();
        apod1.setTitle("Andromeda Galaxy");
        apod1.setExplanation("A beautiful spiral galaxy.");
        Apod apod2 = new Apod();
        apod2.setTitle("Pinwheel Galaxy");
        apod2.setExplanation("Another stunning galaxy.");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.searchApods(searchTerm, null, null, null, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Andromeda Galaxy"))
                .expectNextMatches(response -> response.getTitle().equals("Pinwheel Galaxy"))
                .verifyComplete();

        verify(apodRepository).findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable));
    }

    /**
     * Tests that {@code searchApods} returns an empty Flux when no APODs match the search term.
     */
    @Test
    void searchApods_NoMatchingApods_ShouldReturnEmptyFlux() {
        String searchTerm = "nonexistent";
        Integer size = 10;
        Integer offset = 0;
        Sort sort = Sort.unsorted();
        Pageable pageable = PageRequest.of(offset / size, size, sort);

        when(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(Collections.emptyList());

        Flux<ApodResponse> result = apodService.searchApods(searchTerm, null, null, null, size);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(apodRepository).findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable));
    }

    /**
     * Tests that {@code searchApods} with sort and order parameters returns sorted APODs.
     */
    @Test
    void searchApods_WithSortAndOrder_ShouldReturnSortedApods() {
        String searchTerm = "star";
        Integer size = 2;
        Integer offset = 0;
        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        Pageable pageable = PageRequest.of(offset / size, size, sort);

        Apod apod1 = new Apod();
        apod1.setTitle("Alpha Centauri");
        Apod apod2 = new Apod();
        apod2.setTitle("Betelgeuse Star");
        List<Apod> mockApods = Arrays.asList(apod1, apod2);

        when(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.searchApods(searchTerm, "title", "asc", null, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Alpha Centauri"))
                .expectNextMatches(response -> response.getTitle().equals("Betelgeuse Star"))
                .verifyComplete();

        verify(apodRepository).findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable));
    }

    /**
     * Tests that {@code searchApods} with offset and size parameters returns paginated APODs.
     */
    @Test
    void searchApods_WithOffsetAndSize_ShouldReturnPaginatedApods() {
        String searchTerm = "nebula";
        Integer size = 1;
        Integer offset = 1;
        Sort sort = Sort.unsorted();
        Pageable pageable = PageRequest.of(offset / size, size, sort);

        Apod apod1 = new Apod();
        apod1.setTitle("Orion Nebula");
        Apod apod2 = new Apod();
        apod2.setTitle("Crab Nebula");
        List<Apod> allApods = Arrays.asList(apod1, apod2);
        List<Apod> paginatedApods = Arrays.asList(apod2); // Expected after offset 1, size 1

        when(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(paginatedApods);

        Flux<ApodResponse> result = apodService.searchApods(searchTerm, null, null, offset, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Crab Nebula"))
                .verifyComplete();

        verify(apodRepository).findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable));
    }

    /**
     * Tests that {@code searchApods} with all parameters returns sorted and paginated APODs.
     */
    @Test
    void searchApods_WithAllParams_ShouldReturnSortedAndPaginatedApods() {
        String searchTerm = "planet";
        Integer size = 1;
        Integer offset = 1;
        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        Pageable pageable = PageRequest.of(offset / size, size, sort);

        Apod apod1 = new Apod();
        apod1.setDate(LocalDate.of(2023, 1, 1));
        apod1.setTitle("Mars Planet");
        Apod apod2 = new Apod();
        apod2.setDate(LocalDate.of(2023, 1, 2));
        apod2.setTitle("Jupiter Planet");
        List<Apod> mockApods = Arrays.asList(apod1); // Expected: Jupiter (date 2) then Mars (date 1), offset 1, size 1 -> Mars

        when(apodRepository.findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable)))
                .thenReturn(mockApods);

        Flux<ApodResponse> result = apodService.searchApods(searchTerm, "date", "desc", offset, size);

        StepVerifier.create(result)
                .expectNextMatches(response -> response.getTitle().equals("Mars Planet"))
                .verifyComplete();

        verify(apodRepository).findByTitleContainingIgnoreCaseOrExplanationContainingIgnoreCase(
                eq(searchTerm), eq(searchTerm), eq(pageable));
    }
}