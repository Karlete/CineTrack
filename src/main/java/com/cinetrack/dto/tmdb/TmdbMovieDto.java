// TmdbMovieDto.java
package com.cinetrack.dto.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

public record TmdbMovieDto(
        @JsonProperty("id") Long tmdbId,
        String title,
        @JsonProperty("poster_path") String posterPath,
        @JsonProperty("release_date") String releaseDate,
        String overview
) {}