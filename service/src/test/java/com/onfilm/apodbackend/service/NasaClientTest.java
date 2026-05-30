package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.config.NasaApiConfig;
import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.exception.NasaApiCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawtypes", "unchecked"})
class NasaClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private NasaApiConfig nasaApiConfig;

    @InjectMocks
    private NasaClient nasaClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    @BeforeEach
    void setUp() {
        lenient().when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        lenient().when(requestHeadersUriSpecMock.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpecMock);
        lenient().when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        lenient().when(nasaApiConfig.getBaseUrl()).thenReturn("http://api.nasa.gov");
    }

    @Test
    void fetchApod_ShouldReturnApodResponse() {
        ApodResponse mockResponse = new ApodResponse();
        mockResponse.setTitle("NASA Apod");
        
        when(responseSpecMock.bodyToMono(ApodResponse.class)).thenReturn(Mono.just(mockResponse));

        Mono<ApodResponse> result = nasaClient.fetchApod(LocalDate.of(2023, 10, 1));

        StepVerifier.create(result)
                .expectNextMatches(response -> "NASA Apod".equals(response.getTitle()))
                .verifyComplete();
    }
    
    @Test
    void fetchApod_OnError_ShouldThrowNasaApiCallException() {
        when(responseSpecMock.bodyToMono(ApodResponse.class)).thenReturn(
            Mono.error(new WebClientResponseException(500, "Error", null, null, null))
        );

        Mono<ApodResponse> result = nasaClient.fetchApod(LocalDate.of(2023, 10, 1));

        StepVerifier.create(result)
                .expectError(NasaApiCallException.class)
                .verify();
    }
}