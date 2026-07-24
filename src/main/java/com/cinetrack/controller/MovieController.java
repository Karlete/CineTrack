package com.cinetrack.controller;

import com.cinetrack.dto.MovieSearchResultDto;
import com.cinetrack.dto.WatchedMovieDto;
import com.cinetrack.entities.User;
import com.cinetrack.exceptions.InvalidSearchQueryException;
import com.cinetrack.services.MovieService;
import com.cinetrack.services.TmdbService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    private final TmdbService tmdbService;
    private final MovieService movieService;

    public MovieController(TmdbService tmdbService, MovieService movieService) {
        this.tmdbService = tmdbService;
        this.movieService = movieService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieSearchResultDto>> searchMovies(@RequestParam String query) {
        if (query == null || query.isBlank()) {
            throw new InvalidSearchQueryException("Search query cannot be empty");
        }

        List<MovieSearchResultDto> results = tmdbService.searchMovies(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/{tmdbId}/watched")
    public ResponseEntity<WatchedMovieDto> markAsWatched(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal User user) {
        WatchedMovieDto dto = movieService.markAsWatched(tmdbId, user);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @DeleteMapping("/{tmdbId}/watched")
    public ResponseEntity<Void> markAsNotWatched(
            @PathVariable Long tmdbId,
            @AuthenticationPrincipal User user) {
        movieService.markAsNotWatched(tmdbId, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/watched")
    public ResponseEntity<List<WatchedMovieDto>> getWatchedMovies(@AuthenticationPrincipal User user) {
        List<WatchedMovieDto> watched = movieService.getWatchedMovies(user);
        return ResponseEntity.ok(watched);
    }

    @GetMapping("/popular")
    public ResponseEntity<List<MovieSearchResultDto>> getPopularMovies() {
        List<MovieSearchResultDto> results = tmdbService.getPopularMovies();
        return ResponseEntity.ok(results);
    }
}