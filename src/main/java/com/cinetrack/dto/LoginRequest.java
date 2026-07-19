package com.cinetrack.dto;

public record LoginRequest(
        String userName,
        String password
) {}
