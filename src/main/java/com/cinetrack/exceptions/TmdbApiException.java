package com.cinetrack.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when there's an error communicating with the TMDB API.
 * Carries the original status for logging/debug purposes.
 */
public class TmdbApiException extends RuntimeException{
    private final HttpStatus status;

    public TmdbApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
