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
                        // Rutas de autenticación (API + vistas)
                        .requestMatchers("/auth/**").permitAll()

                        // Búsqueda (API + vista)
                        .requestMatchers("/movies/search").permitAll()

                        .requestMatchers("/movies/popular").permitAll()

                        // Vistas Thymeleaf públicas
                        .requestMatchers("/", "/login", "/register", "/search", "/watched").permitAll()

                        // Recursos estáticos
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Add JWT filter before default authentication
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}