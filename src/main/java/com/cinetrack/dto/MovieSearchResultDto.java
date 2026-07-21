package com.cinetrack.dto;

public record MovieSearchResultDto(
        Long tmdbId,
        String title,
        String posterPath,
        Integer year,
        String overview
) {}
