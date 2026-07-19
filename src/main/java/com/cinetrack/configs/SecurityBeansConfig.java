package com.cinetrack.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class for security-related beans.
 * This keeps security beans organized separately from the main SecurityFilterChain.
 */
@Configuration
public class SecurityBeansConfig {

    /**
     * Provides the AuthenticationManager bean.
     * This is used by the AuthService to authenticate users during login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    /**
     * Password encoder using BCrypt (recommended for production).
     * Strength 12 offers a good balance between security and performance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}


