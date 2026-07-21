package com.cinetrack.services;

import com.cinetrack.dto.WatchedMovieDto;
import com.cinetrack.dto.tmdb.TmdbMovieDetailsDto;
import com.cinetrack.entities.Movie;
import com.cinetrack.entities.User;
import com.cinetrack.entities.WatchedMovie;
import com.cinetrack.repositories.MovieRepository;
import com.cinetrack.repositories.WatchedMovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovieService {

    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);

    private final MovieRepository movieRepository;
    private final WatchedMovieRepository watchedMovieRepository;
    private final TmdbService tmdbService;

    public MovieService(MovieRepository movieRepository,
                        WatchedMovieRepository watchedMovieRepository,
                        TmdbService tmdbService) {
        this.movieRepository = movieRepository;
        this.watchedMovieRepository = watchedMovieRepository;
        this.tmdbService = tmdbService;
    }

    @Transactional
    public WatchedMovieDto markAsWatched(Long tmdbId, User user) {
        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseGet(() -> createMovieFromTmdb(tmdbId));

        var existing = watchedMovieRepository.findByUserAndMovie_TmdbId(user, tmdbId);
        if (existing.isPresent()) {
            return buildWatchedMovieDto(movie, existing.get());
        }

        WatchedMovie watchedMovie = new WatchedMovie();
        watchedMovie.setUser(user);
        watchedMovie.setMovie(movie);
        watchedMovie.setWatchedAt(LocalDateTime.now());

        WatchedMovie saved = watchedMovieRepository.save(watchedMovie);

        return buildWatchedMovieDto(movie, saved);
    }

    @Transactional
    public void markAsNotWatched(Long tmdbId, User user) {
        watchedMovieRepository.findByUserAndMovie_TmdbId(user, tmdbId)
                .ifPresent(watchedMovieRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<WatchedMovieDto> getWatchedMovies(User user) {
        return watchedMovieRepository.findByUser(user).stream()
                .map(wm -> buildWatchedMovieDto(wm.getMovie(), wm))
                .toList();
    }

    private Movie createMovieFromTmdb(Long tmdbId) {
        TmdbMovieDetailsDto dto = tmdbService.getMovieDetails(tmdbId);

        Movie movie = new Movie();
        movie.setTmdbId(dto.tmdbId());
        movie.setTitle(dto.title());
        movie.setPosterPath(dto.posterPath());
        movie.setOverview(dto.overview());

        if (dto.releaseDate() != null && !dto.releaseDate().isBlank()) {
            try {
                movie.setReleaseDate(LocalDate.parse(dto.releaseDate()));
            } catch (Exception e) {
                logger.warn("Failed to parse release date for movie {}: {}", tmdbId, dto.releaseDate());
            }
        }

        return movieRepository.save(movie);
    }

    private WatchedMovieDto buildWatchedMovieDto(Movie movie, WatchedMovie watchedMovie) {
        Integer year = movie.getReleaseDate() != null ? movie.getReleaseDate().getYear() : null;

        return new WatchedMovieDto(
                movie.getTmdbId(),
                movie.getTitle(),
                movie.getPosterPath(),
                year,
                watchedMovie.getWatchedAt()
        );
    }
}