package com.onfilm.apodbackend.controller;

import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.service.ApodService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(ApodController.class)
class ApodControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApodService apodService;

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

    @Test
    void getApod_WithoutDate_ShouldReturnApodResponseForToday() {
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("Today Title");
        mockResponse.setDate(LocalDate.now());

        when(apodService.getApod(any(LocalDate.class))).thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/v1/apod")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Today Title")
                .jsonPath("$.date").isEqualTo(LocalDate.now().toString());
    }

    @Test
    void getApod_ServiceThrowsException_ShouldReturnInternalServerError() {
        when(apodService.getApod(any(LocalDate.class))).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        webTestClient.get()
                .uri("/api/v1/apod")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.error").isEqualTo("Internal Server Error")
                .jsonPath("$.message").isEqualTo("Service unavailable");
    }
}
