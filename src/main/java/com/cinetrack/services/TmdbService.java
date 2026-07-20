package com.cinetrack.services;

import com.cinetrack.dto.tmdb.TmdbMovieDto;
import com.cinetrack.dto.tmdb.TmdbSearchResponse;
import com.cinetrack.exceptions.TmdbApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;


/**
 * Service responsible for interacting with the TMDB API.
 * Handles movie search operations.
 */
@Service
public class TmdbService {
    private static final Logger logger = LoggerFactory.getLogger(TmdbService.class);

    private final RestClient tmdbRestClient;

    public TmdbService(RestClient tmdbRestClient) {
        this.tmdbRestClient = tmdbRestClient;
    }

    /**
     * Searches for movies in TMDB by query string.
     * Returns a list of movie results (read-only, no persistence).
     */
    public List<TmdbMovieDto> searchMovies(String query) {
        try {
            TmdbSearchResponse response = tmdbRestClient
                    .get()
                    .uri("/search/movie?query={query}&language=es-ES", query)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response1) -> {
                        throw new TmdbApiException("TMDB client error: " + response1.getStatusCode(),
                                HttpStatus.valueOf(response1.getStatusCode().value()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response1) -> {
                        throw new TmdbApiException("TMDB server error", HttpStatus.SERVICE_UNAVAILABLE);
                    })
                    .body(TmdbSearchResponse.class);

            return response != null ? response.results() : List.of();

        } catch (ResourceAccessException e) {
            logger.warn("Network error connecting to TMDB for query: {}", query, e);
            throw new TmdbApiException("Network error connecting to TMDB", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (TmdbApiException e) {
            logger.warn("TMDB API error for query '{}': {} - {}", query, e.getStatus(), e.getMessage());
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            logger.error("Unexpected error searching movies with query: {}", query, e);
            throw new TmdbApiException("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
