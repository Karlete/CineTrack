package com.cinetrack.repositories;

import com.cinetrack.entities.User;
import com.cinetrack.entities.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, Long> {

    Optional<WatchedMovie> findByUserAndMovie_TmdbId(User user, Long tmdbId);

    List<WatchedMovie> findByUser(User user);
}
