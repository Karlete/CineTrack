package com.cinetrack.dto;

import java.time.LocalDateTime;

public record WatchedMovieDto(
        Long tmdbId,
        String title,
        String posterPath,
        Integer year,
        LocalDateTime watchedAt
) {}