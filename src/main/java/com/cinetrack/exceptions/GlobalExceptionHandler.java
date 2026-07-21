package com.cinetrack.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles duplicate user registration attempts.
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        var error = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage()
        );

        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    /**
     * Handles invalid login credentials.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex) {
        var error = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.UNAUTHORIZED.value(),
                "error", "Unauthorized",
                "message", "Invalid username or password"
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(TmdbApiException.class)
    public ResponseEntity<Object> handleTmdbApiException(TmdbApiException ex) {
        String userMessage = switch (ex.getStatus()) {
            case TOO_MANY_REQUESTS -> "Demasiadas peticiones. Espera un poco e inténtalo de nuevo.";
            case UNAUTHORIZED -> "Error de autenticación con el servicio externo.";
            default -> "No se pudo conectar con el servicio de películas. Inténtalo de nuevo más tarde.";
        };

        var error = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error", "Service Unavailable",
                "message", userMessage
        );

        return new ResponseEntity<>(error, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InvalidSearchQueryException.class)
    public ResponseEntity<Object> handleInvalidSearchQuery(InvalidSearchQueryException ex) {
        var error = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Bad Request",
                "message", ex.getMessage()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
