package com.cinetrack.services;

import com.cinetrack.dto.LoginRequest;
import com.cinetrack.dto.LoginResponse;
import com.cinetrack.dto.RegisterRequest;
import com.cinetrack.dto.RegisterResponse;
import com.cinetrack.entities.User;
import com.cinetrack.exceptions.UserAlreadyExistsException;
import com.cinetrack.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsUserByUserName(request.userName())) {
            throw new UserAlreadyExistsException("The username '" + request.userName() + "' is already taken.");
        }

        if (userRepository.existsUserByEmail(request.email())) {
            throw new UserAlreadyExistsException("The email '" + request.email() + "' is already in use.");
        }

        User user = new User();
        user.setUserName(request.userName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        return new RegisterResponse(savedUser.getId(), savedUser.getUserName(), savedUser.getEmail());
    }

    /**
     * Login using AuthenticationManager
     */
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.userName(), request.password())
            );

            User user = (User) authentication.getPrincipal();

            // Update last login
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            String token = jwtService.generateToken(user);

            return new LoginResponse(token, user.getUserName(), user.getId());

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }
}
