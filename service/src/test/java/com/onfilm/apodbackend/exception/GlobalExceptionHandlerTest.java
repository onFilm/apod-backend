package com.onfilm.apodbackend.exception;

import com.onfilm.apodbackend.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private ServerWebExchange serverWebExchange;

    @Mock
    private ServerHttpRequest serverHttpRequest;

    @Mock
    private RequestPath requestPath;

    @BeforeEach
    void setUp() {
        when(serverWebExchange.getRequest()).thenReturn(serverHttpRequest);
        when(serverHttpRequest.getPath()).thenReturn(requestPath);
        when(requestPath.value()).thenReturn("/test-path");
    }

    @Test
    void handleNasaApiCallException_shouldReturnInternalServerError() {
        NasaApiCallException ex = new NasaApiCallException("NASA API is down");

        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleNasaApiCallException(ex, serverWebExchange);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatus());
        assertEquals("NASA API Error", responseEntity.getBody().getError());
        assertEquals("NASA API is down", responseEntity.getBody().getMessage());
        assertEquals("/test-path", responseEntity.getBody().getPath());
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        Exception ex = new Exception("Something went wrong");

        ResponseEntity<ErrorResponse> responseEntity = globalExceptionHandler.handleGenericException(ex, serverWebExchange);

        assertNotNull(responseEntity);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), responseEntity.getBody().getStatus());
        assertEquals("Internal Server Error", responseEntity.getBody().getError());
        assertEquals("Something went wrong", responseEntity.getBody().getMessage());
        assertEquals("/test-path", responseEntity.getBody().getPath());
    }
}
