package com.cinetrack.dto;

public record MovieDetailDto(
        Long tmdbId,
        String title,
        String posterPath,
        Integer year,
        String overview,
        String director
) {}
