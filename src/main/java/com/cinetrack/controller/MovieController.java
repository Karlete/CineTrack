package com.cinetrack.controller;

import com.cinetrack.dto.MovieSearchResultDto;
import com.cinetrack.exceptions.InvalidSearchQueryException;
import com.cinetrack.services.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for movie-related endpoints.
 * Currently handles movie search via TMDB.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

    private final TmdbService tmdbService;

    public MovieController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    /**
     * Searches movies by query string.
     */
    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResultDto>> searchMovies(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidSearchQueryException("Search query cannot be empty");
        }

        List<MovieSearchResultDto> results = tmdbService.searchMovies(query);
        return ResponseEntity.ok(results);
    }
}
