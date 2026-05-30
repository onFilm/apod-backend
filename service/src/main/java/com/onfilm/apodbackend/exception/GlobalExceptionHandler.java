package com.onfilm.apodbackend.exception;

import com.onfilm.apodbackend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import java.time.LocalDateTime;

/**
 * Global exception handler for the application.
 * This class catches exceptions thrown by controllers and provides appropriate error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles exceptions specifically related to NASA API calls.
     *
     * @param ex The NasaApiCallException that was thrown.
     * @param exchange The current server web exchange.
     * @return A ResponseEntity containing an ErrorResponse with details about the exception.
     */
    @ExceptionHandler(NasaApiCallException.class)
    public ResponseEntity<ErrorResponse> handleNasaApiCallException(NasaApiCallException ex, ServerWebExchange exchange) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "NASA API Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles generic exceptions not specifically caught by other handlers.
     *
     * @param ex The Exception that was thrown.
     * @param exchange The current server web exchange.
     * @return A ResponseEntity containing an ErrorResponse with details about the exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, ServerWebExchange exchange) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                exchange.getRequest().getPath().value()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}