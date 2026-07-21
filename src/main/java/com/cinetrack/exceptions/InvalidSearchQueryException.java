package com.cinetrack.exceptions;

/**
 * Thrown when the search query is invalid (empty or blank).
 */
public class InvalidSearchQueryException extends RuntimeException {

    public InvalidSearchQueryException(String message) {
        super(message);
    }
}
