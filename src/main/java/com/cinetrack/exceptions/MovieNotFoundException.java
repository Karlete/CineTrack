package com.cinetrack.exceptions;

/**
 * Thrown when a movie is not found in TMDB by its tmdbId.
 */
public class MovieNotFoundException extends RuntimeException {

    public MovieNotFoundException(String message) {
        super(message);
    }
}
