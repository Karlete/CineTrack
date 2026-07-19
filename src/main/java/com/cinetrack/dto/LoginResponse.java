package com.cinetrack.dto;

public record LoginResponse(
        String token,
        String userName,
        Long userId
) {}
