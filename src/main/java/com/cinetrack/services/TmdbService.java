package com.cinetrack.services;

import com.cinetrack.dto.MovieSearchResultDto;
import com.cinetrack.dto.tmdb.TmdbMovieDetailsDto;
import com.cinetrack.dto.tmdb.TmdbMovieDto;
import com.cinetrack.dto.tmdb.TmdbSearchResponse;
import com.cinetrack.exceptions.MovieNotFoundException;
import com.cinetrack.exceptions.TmdbApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;

@Service
public class TmdbService {

    private static final Logger logger = LoggerFactory.getLogger(TmdbService.class);

    private final RestClient tmdbRestClient;

    public TmdbService(RestClient tmdbRestClient) {
        this.tmdbRestClient = tmdbRestClient;
    }

    /**
     * Searches for movies in TMDB and returns results in our API format.
     */
    public List<MovieSearchResultDto> searchMovies(String query) {
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

            if (response == null || response.results() == null) {
                return List.of();
            }

            return response.results().stream()
                    .map(this::toMovieSearchResult)
                    .toList();

        } catch (ResourceAccessException e) {
            logger.warn("Network error connecting to TMDB for query: {}", query, e);
            throw new TmdbApiException("Network error connecting to TMDB", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (TmdbApiException e) {
            logger.warn("TMDB API error for query '{}': {} - {}", query, e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error searching movies with query: {}", query, e);
            throw new TmdbApiException("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private MovieSearchResultDto toMovieSearchResult(TmdbMovieDto dto) {
        Integer year = null;
        if (dto.releaseDate() != null && !dto.releaseDate().isBlank()) {
            try {
                LocalDate date = LocalDate.parse(dto.releaseDate());
                year = date.getYear();
            } catch (Exception e) {
                logger.warn("Failed to parse release date for TMDB movie {}: {}", dto.tmdbId(), dto.releaseDate());
            }
        }

        return new MovieSearchResultDto(
                dto.tmdbId(),
                dto.title(),
                dto.posterPath(),
                year,
                dto.overview()
        );
    }

    /**
     * Gets detailed information for a specific movie by its TMDB ID.
     */
    public TmdbMovieDetailsDto getMovieDetails(Long tmdbId) {
        try {
            return tmdbRestClient
                    .get()
                    .uri("/movie/{tmdbId}?language=es-ES", tmdbId)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                        if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                            throw new MovieNotFoundException("Movie with TMDB ID " + tmdbId + " not found");
                        }
                        throw new TmdbApiException("TMDB client error: " + response.getStatusCode(),
                                HttpStatus.valueOf(response.getStatusCode().value()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
                        throw new TmdbApiException("TMDB server error", HttpStatus.SERVICE_UNAVAILABLE);
                    })
                    .body(TmdbMovieDetailsDto.class);

        } catch (ResourceAccessException e) {
            logger.warn("Network error fetching movie details for tmdbId: {}", tmdbId, e);
            throw new TmdbApiException("Network error connecting to TMDB", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (TmdbApiException | MovieNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching movie details for tmdbId: {}", tmdbId, e);
            throw new TmdbApiException("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Fetches the first page of popular movies from TMDB and returns results in our API format.
     */
    public List<MovieSearchResultDto> getPopularMovies() {
        try {
            TmdbSearchResponse response = tmdbRestClient
                    .get()
                    .uri("/movie/popular?language=es-ES&page=1")
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, response1) -> {
                        throw new TmdbApiException("TMDB client error: " + response1.getStatusCode(),
                                HttpStatus.valueOf(response1.getStatusCode().value()));
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, (request, response1) -> {
                        throw new TmdbApiException("TMDB server error", HttpStatus.SERVICE_UNAVAILABLE);
                    })
                    .body(TmdbSearchResponse.class);

            if (response == null || response.results() == null) {
                return List.of();
            }

            return response.results().stream()
                    .map(this::toMovieSearchResult)
                    .toList();

        } catch (ResourceAccessException e) {
            logger.warn("Network error connecting to TMDB for popular movies", e);
            throw new TmdbApiException("Network error connecting to TMDB", HttpStatus.SERVICE_UNAVAILABLE);
        } catch (TmdbApiException e) {
            logger.warn("TMDB API error for popular movies: {} - {}", e.getStatus(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching popular movies", e);
            throw new TmdbApiException("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}