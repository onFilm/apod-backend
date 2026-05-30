package com.onfilm.apodbackend.dto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class ErrorResponseTest {

    @Test
    void testNoArgsConstructor() {
        ErrorResponse errorResponse = new ErrorResponse();
        assertNotNull(errorResponse);
    }

    @Test
    void testAllArgsConstructorAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        int status = 500;
        String error = "Internal Server Error";
        String message = "Error message";
        String path = "/api/apod";

        ErrorResponse errorResponse = new ErrorResponse(timestamp, status, error, message, path);

        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    @Test
    void testSetters() {
        ErrorResponse errorResponse = new ErrorResponse();

        LocalDateTime timestamp = LocalDateTime.now();
        int status = 404;
        String error = "Not Found";
        String message = "Another error message";
        String path = "/api/nonexistent";

        errorResponse.setTimestamp(timestamp);
        errorResponse.setStatus(status);
        errorResponse.setError(error);
        errorResponse.setMessage(message);
        errorResponse.setPath(path);

        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(status, errorResponse.getStatus());
        assertEquals(error, errorResponse.getError());
        assertEquals(message, errorResponse.getMessage());
        assertEquals(path, errorResponse.getPath());
    }

    @Test
    void testEqualsAndHashCode() {
        LocalDateTime timestamp = LocalDateTime.now();
        ErrorResponse errorResponse1 = new ErrorResponse(timestamp, 500, "Error", "Message", "/path");
        ErrorResponse errorResponse2 = new ErrorResponse(timestamp, 500, "Error", "Message", "/path");
        ErrorResponse errorResponse3 = new ErrorResponse(LocalDateTime.now().plusHours(1), 400, "Different Error", "Different Message", "/anotherpath");

        assertEquals(errorResponse1, errorResponse2);
        assertNotEquals(errorResponse1, errorResponse3);
        assertEquals(errorResponse1.hashCode(), errorResponse2.hashCode());
        // Note: HashCode for LocalDateTime can be different even if values are close,
        // so we don't assert inequality for hash codes of objects with different timestamps.
    }

    @Test
    void testToString() {
        LocalDateTime timestamp = LocalDateTime.of(2023, 1, 1, 10, 0, 0);
        ErrorResponse errorResponse = new ErrorResponse(timestamp, 500, "Error", "Message", "/path");
        String expectedToString = "ErrorResponse(timestamp=2023-01-01T10:00, status=500, error=Error, message=Message, path=/path)";
        assertEquals(expectedToString, errorResponse.toString());
    }
}
