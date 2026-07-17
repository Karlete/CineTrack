package com.cinetrack.repositories;

import com.cinetrack.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByUserName(String userName);

    boolean existsUserByUserName(String userName);
    boolean existsUserByEmail(String email);
}
