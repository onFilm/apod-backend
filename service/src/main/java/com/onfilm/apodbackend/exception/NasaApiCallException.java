package com.onfilm.apodbackend.exception;

/**
 * Exception thrown when an error occurs during a NASA API call.
 */
public class NasaApiCallException extends RuntimeException {

    /**
     * Constructs a new NasaApiCallException with the specified detail message.
     * @param message the detail message.
     */
    public NasaApiCallException(String message) {
        super(message);
    }

    /**
     * Constructs a new NasaApiCallException with the specified detail message and cause.
     * @param message the detail message.
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
     */
    public NasaApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}