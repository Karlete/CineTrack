package com.cinetrack.dto;

public record RegisterResponse(
        Long userId,
        String userName,
        String email
) {}
