package com.cinetrack.configs;

import com.cinetrack.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF because we use stateless JWT
                .csrf(AbstractHttpConfigurer::disable)

                // No sessions needed
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Auth routes (API + views)
                        .requestMatchers("/auth/**").permitAll()

                        // Search (API + view)
                        .requestMatchers("/movies/search").permitAll()

                        .requestMatchers("/movies/popular").permitAll()

                        // Public Thymeleaf views
                        .requestMatchers("/", "/login", "/register", "/search", "/watched").permitAll()

                        // Movie detail page (public view, accessible without login: bookmark, direct link, F5).
                        // Single segment ("*", not "**"): the real route is /movie/{tmdbId}, no sub-routes.
                        .requestMatchers("/movie/*").permitAll()

                        // Static resources
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // Add JWT filter before default authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}