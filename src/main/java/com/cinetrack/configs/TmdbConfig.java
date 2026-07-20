package com.cinetrack.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Configuration class for external RestClients.
 * Currently configures only the TMDB client.
 */
@Configuration
public class TmdbConfig {
    @Value("${tmdb.base-url}")
    private String tmdbBaseUrl;

    @Value("${tmdb.api-key}")
    private String tmdbApiKey;

    /**
     * RestClient bean configured for TMDB API v4 (Bearer token authentication).
     */
    @Bean
    public RestClient tmdbRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseUrl)
                .defaultHeaders(headers -> headers.setBearerAuth(tmdbApiKey))
                .build();
    }
}
