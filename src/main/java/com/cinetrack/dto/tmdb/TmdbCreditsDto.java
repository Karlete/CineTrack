package com.cinetrack.dto.tmdb;

import java.util.List;

public record TmdbCreditsDto(
        List<TmdbCrewMemberDto> crew
) {}
