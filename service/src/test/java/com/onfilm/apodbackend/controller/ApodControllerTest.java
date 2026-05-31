package com.onfilm.apodbackend.controller;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.service.ApodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the {@link ApodController} class.
 */
@WebFluxTest(ApodController.class)
class ApodControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApodService apodService;

    /**
     * Tests that {@code getApod} with a date parameter returns an {@link ApodResponse}.
     */
    @Test
    void getApod_WithDate_ShouldReturnApodResponse() {
        LocalDate date = LocalDate.of(2023, 10, 1);
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("Test Title");
        mockResponse.setDate(date);

        when(apodService.getApod(date)).thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/apod?date=2023-10-01")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Test Title")
                .jsonPath("$.date").isEqualTo("2023-10-01");
    }

    /**
     * Tests that {@code getApod} returns an internal server error when the service throws an exception.
     */
    @Test
    void getApod_ServiceThrowsException_ShouldReturnInternalServerError() {
        when(apodService.getApod(any(LocalDate.class))).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apod")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Service unavailable");
    }

    /**
     * Tests that {@code getApods} with no parameters returns a list of {@link ApodResponse}.
     */
    @Test
    void getApods_NoParams_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.getApods(isNull(), isNull(), isNull(), isNull())).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code getApods} with sort and order parameters returns a list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSortAndOrder_ShouldReturnListOfApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1, apod2);

        when(apodService.getApods(eq("date"), eq("asc"), isNull(), isNull())).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_sort=date&_order=asc")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(2)
                .contains(apod1, apod2);
    }

    /**
     * Tests that {@code getApods} with offset parameter returns a paginated list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithOffset_ShouldReturnPaginatedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod2); // Only apod2 after offset 1

        when(apodService.getApods(isNull(), isNull(), eq(1), isNull())).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_offset=1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod2);
    }

    /**
     * Tests that {@code getApods} with size parameter returns a limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSize_ShouldReturnLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1); // Only apod1 with size 1

        when(apodService.getApods(isNull(), isNull(), isNull(), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_size=1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod1);
    }

    /**
     * Tests that {@code getApods} with offset and size parameters returns a paginated and limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithOffsetAndSize_ShouldReturnPaginatedAndLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod2); // Only apod2 with offset 1 and size 1

        when(apodService.getApods(isNull(), isNull(), eq(1), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_offset=1&_size=1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod2);
    }

    /**
     * Tests that {@code getApods} with sort, order, offset, and size parameters returns a sorted, paginated, and limited list of {@link ApodResponse}.
     */
    @Test
    void getApods_WithSortOrderOffsetAndSize_ShouldReturnSortedPaginatedAndLimitedApodResponses() {
        ApodResponse apod1 = new ApodResponse();
        apod1.setTitle("Apod 1");
        apod1.setDate(LocalDate.of(2023, 1, 1));
        ApodResponse apod2 = new ApodResponse();
        apod2.setTitle("Apod 2");
        apod2.setDate(LocalDate.of(2023, 1, 2));
        List<ApodResponse> mockResponses = Arrays.asList(apod1); // Assuming sorted by date desc, offset 1, size 1 would give apod1

        when(apodService.getApods(eq("date"), eq("desc"), eq(1), eq(1))).thenReturn(Flux.fromIterable(mockResponses));

        webTestClient.get()
                .uri("/api/v1/apods?_sort=date&_order=desc&_offset=1&_size=1")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApodResponse.class)
                .hasSize(1)
                .contains(apod1);
    }

    /**
     * Tests that {@code getApods} returns an internal server error when the service throws an exception.
     */
    @Test
    void getApods_ServiceThrowsException_ShouldReturnInternalServerError() {
        when(apodService.getApods(isNull(), isNull(), isNull(), isNull())).thenReturn(Flux.error(new RuntimeException("Apods service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apods")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Apods service unavailable");
    }
}