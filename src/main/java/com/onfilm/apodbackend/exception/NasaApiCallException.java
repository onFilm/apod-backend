package com.onfilm.apodbackend.exception;

public class NasaApiCallException extends RuntimeException {

    public NasaApiCallException(String message) {
        super(message);
    }

    public NasaApiCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
