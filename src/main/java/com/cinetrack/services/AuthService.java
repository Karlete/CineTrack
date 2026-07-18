package com.cinetrack.services;

import com.cinetrack.entities.User;
import com.cinetrack.exceptions.UserAlreadyExistsException;
import com.cinetrack.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(String userName, String email, String password){
        // Validate user
        if (userRepository.existsUserByUserName(userName)) {
            throw new UserAlreadyExistsException("The username '" + userName + "' is already taken.");
        }

        if(userRepository.existsUserByEmail(email)){
            throw new UserAlreadyExistsException("The email '" + email + "' is in use.");
        }

        // Create and save user
        User user = new User();
        user.setUserName(userName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
