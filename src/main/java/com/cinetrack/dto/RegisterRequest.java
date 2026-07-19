package com.cinetrack.dto;

public record RegisterRequest(
        String userName,
        String email,
        String password
) {}
