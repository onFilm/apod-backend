package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.dto.ApodResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApodServiceImplTest {

    @Mock
    private NasaClient nasaClient;

    @InjectMocks
    private ApodServiceImpl apodService;

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
}
