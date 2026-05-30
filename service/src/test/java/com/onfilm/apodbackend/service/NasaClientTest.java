package com.onfilm.apodbackend.service;

import com.onfilm.apodbackend.config.NasaApiConfig;
import com.onfilm.apodbackend.dto.ApodResponse;
import com.onfilm.apodbackend.exception.NasaApiCallException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import java.net.URI;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void fetchApod_ShouldBuildCorrectUriWithDate() {
        LocalDate testDate = LocalDate.of(2023, 1, 15);
        String expectedDateParam = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

        // Mock the response to allow the call to proceed
        ApodResponse mockResponse = new ApodResponse();
        when(responseSpecMock.bodyToMono(ApodResponse.class)).thenReturn(Mono.just(mockResponse));

        // Call the method under test
        nasaClient.fetchApod(testDate).subscribe(); // Use subscribe to trigger the WebClient call

        // Capture the URI builder function
        ArgumentCaptor<Function<UriBuilder, URI>> uriFunctionCaptor = ArgumentCaptor.forClass(Function.class);
        verify(requestHeadersUriSpecMock).uri(anyString(), uriFunctionCaptor.capture());

        // Create a mock UriBuilder and apply the captured function to it
        UriBuilder uriBuilderMock = mock(UriBuilder.class);
        when(uriBuilderMock.queryParam(anyString(), anyString())).thenReturn(uriBuilderMock);
        when(uriBuilderMock.build()).thenReturn(URI.create("http://api.nasa.gov?date=" + expectedDateParam)); // Return a dummy URI

        uriFunctionCaptor.getValue().apply(uriBuilderMock);

        // Verify that queryParam was called with the correct arguments
        verify(uriBuilderMock).queryParam("date", expectedDateParam);
        verify(uriBuilderMock).build();
    }
}
