package com.cinetrack.services;

import com.cinetrack.dto.WatchedMovieDto;
import com.cinetrack.dto.tmdb.TmdbMovieDetailsDto;
import com.cinetrack.entities.Movie;
import com.cinetrack.entities.User;
import com.cinetrack.entities.WatchedMovie;
import com.cinetrack.exceptions.MovieNotFoundException;
import com.cinetrack.exceptions.TmdbApiException;
import com.cinetrack.repositories.MovieRepository;
import com.cinetrack.repositories.WatchedMovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * MovieService no accede a SecurityContextHolder (recibe User explícito), lo que lo hace
 * directamente testeable con Mockito puro, sin necesidad de levantar contexto de Spring.
 */
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private WatchedMovieRepository watchedMovieRepository;

    @Mock
    private TmdbService tmdbService;

    private MovieService movieService;

    private User user;
    private static final Long TMDB_ID = 550L;

    @BeforeEach
    void setUp() {
        movieService = new MovieService(movieRepository, watchedMovieRepository, tmdbService);

        user = new User();
        user.setId(1L);
        user.setUserName("neo");
    }

    // ==================== markAsWatched ====================

    @Test
    void markAsWatched_newMovie_fetchesFromTmdbAndPersistsMovieAndWatchedEntry() {
        TmdbMovieDetailsDto tmdbDto = new TmdbMovieDetailsDto(
                TMDB_ID, "Fight Club", "/poster.jpg", "1999-10-15", "An insomniac office worker...");

        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.empty());
        when(tmdbService.getMovieDetails(TMDB_ID)).thenReturn(tmdbDto);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.empty());
        when(watchedMovieRepository.save(any(WatchedMovie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WatchedMovieDto result = movieService.markAsWatched(TMDB_ID, user);

        assertThat(result.tmdbId()).isEqualTo(TMDB_ID);
        assertThat(result.title()).isEqualTo("Fight Club");
        assertThat(result.posterPath()).isEqualTo("/poster.jpg");
        assertThat(result.year()).isEqualTo(1999);
        assertThat(result.watchedAt()).isNotNull();

        ArgumentCaptor<Movie> movieCaptor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(movieCaptor.capture());
        Movie savedMovie = movieCaptor.getValue();
        assertThat(savedMovie.getTmdbId()).isEqualTo(TMDB_ID);
        assertThat(savedMovie.getOverview()).isEqualTo("An insomniac office worker...");
        assertThat(savedMovie.getReleaseDate()).isEqualTo(LocalDate.of(1999, 10, 15));

        ArgumentCaptor<WatchedMovie> watchedCaptor = ArgumentCaptor.forClass(WatchedMovie.class);
        verify(watchedMovieRepository).save(watchedCaptor.capture());
        assertThat(watchedCaptor.getValue().getUser()).isEqualTo(user);
        assertThat(watchedCaptor.getValue().getMovie()).isEqualTo(savedMovie);
    }

    @Test
    void markAsWatched_movieAlreadyCached_doesNotCallTmdb() {
        Movie cachedMovie = new Movie();
        cachedMovie.setTmdbId(TMDB_ID);
        cachedMovie.setTitle("Fight Club");

        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.of(cachedMovie));
        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.empty());
        when(watchedMovieRepository.save(any(WatchedMovie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WatchedMovieDto result = movieService.markAsWatched(TMDB_ID, user);

        assertThat(result.title()).isEqualTo("Fight Club");
        verifyNoInteractions(tmdbService);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void markAsWatched_alreadyMarkedAsWatched_returnsExistingEntryWithoutDuplicating() {
        Movie cachedMovie = new Movie();
        cachedMovie.setTmdbId(TMDB_ID);
        cachedMovie.setTitle("Fight Club");
        cachedMovie.setPosterPath("/poster.jpg");

        LocalDateTime originalWatchedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        WatchedMovie existing = new WatchedMovie();
        existing.setUser(user);
        existing.setMovie(cachedMovie);
        existing.setWatchedAt(originalWatchedAt);

        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.of(cachedMovie));
        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.of(existing));

        WatchedMovieDto result = movieService.markAsWatched(TMDB_ID, user);

        assertThat(result.watchedAt()).isEqualTo(originalWatchedAt);
        verify(watchedMovieRepository, never()).save(any());
        verifyNoInteractions(tmdbService);
    }

    @Test
    void markAsWatched_movieNotFoundInTmdb_propagatesExceptionWithoutPersisting() {
        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.empty());
        when(tmdbService.getMovieDetails(TMDB_ID)).thenThrow(new MovieNotFoundException("not found"));

        assertThatThrownBy(() -> movieService.markAsWatched(TMDB_ID, user))
                .isInstanceOf(MovieNotFoundException.class);

        verify(movieRepository, never()).save(any());
        verifyNoInteractions(watchedMovieRepository);
    }

    @Test
    void markAsWatched_tmdbApiFails_propagatesExceptionWithoutPersisting() {
        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.empty());
        when(tmdbService.getMovieDetails(TMDB_ID))
                .thenThrow(new TmdbApiException("TMDB down", HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> movieService.markAsWatched(TMDB_ID, user))
                .isInstanceOf(TmdbApiException.class);

        verify(movieRepository, never()).save(any());
        verifyNoInteractions(watchedMovieRepository);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   ", "not-a-date"})
    void markAsWatched_defensiveReleaseDateParsing_neverBreaksTheFlow(String releaseDate) {
        TmdbMovieDetailsDto tmdbDto = new TmdbMovieDetailsDto(
                TMDB_ID, "Fight Club", "/poster.jpg", releaseDate, "overview");

        when(movieRepository.findByTmdbId(TMDB_ID)).thenReturn(Optional.empty());
        when(tmdbService.getMovieDetails(TMDB_ID)).thenReturn(tmdbDto);
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.empty());
        when(watchedMovieRepository.save(any(WatchedMovie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // No debe lanzar excepción aunque el parseo de la fecha falle (parseo defensivo)
        WatchedMovieDto result = movieService.markAsWatched(TMDB_ID, user);

        assertThat(result.year()).isNull();
    }

    // ==================== markAsNotWatched ====================

    @Test
    void markAsNotWatched_existingEntry_deletesIt() {
        WatchedMovie existing = new WatchedMovie();
        existing.setId(42L);

        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.of(existing));

        movieService.markAsNotWatched(TMDB_ID, user);

        verify(watchedMovieRepository, times(1)).delete(existing);
    }

    @Test
    void markAsNotWatched_nonExistingEntry_isNoOp() {
        when(watchedMovieRepository.findByUserAndMovie_TmdbId(user, TMDB_ID)).thenReturn(Optional.empty());

        assertThatCode(() -> movieService.markAsNotWatched(TMDB_ID, user)).doesNotThrowAnyException();

        verify(watchedMovieRepository, never()).delete(any());
    }

    // ==================== getWatchedMovies ====================

    @Test
    void getWatchedMovies_noEntries_returnsEmptyList() {
        when(watchedMovieRepository.findByUser(user)).thenReturn(List.of());

        List<WatchedMovieDto> result = movieService.getWatchedMovies(user);

        assertThat(result).isEmpty();
    }

    @Test
    void getWatchedMovies_withEntries_mapsToDtoIncludingWatchedAt() {
        Movie movie1 = new Movie();
        movie1.setTmdbId(1L);
        movie1.setTitle("Movie One");
        movie1.setPosterPath("/one.jpg");
        movie1.setReleaseDate(LocalDate.of(2010, 5, 20));

        Movie movie2 = new Movie();
        movie2.setTmdbId(2L);
        movie2.setTitle("Movie Two");
        movie2.setReleaseDate(null);

        LocalDateTime watchedAt1 = LocalDateTime.of(2024, 3, 1, 12, 0);
        LocalDateTime watchedAt2 = LocalDateTime.of(2024, 3, 2, 9, 30);

        WatchedMovie wm1 = new WatchedMovie();
        wm1.setMovie(movie1);
        wm1.setUser(user);
        wm1.setWatchedAt(watchedAt1);

        WatchedMovie wm2 = new WatchedMovie();
        wm2.setMovie(movie2);
        wm2.setUser(user);
        wm2.setWatchedAt(watchedAt2);

        when(watchedMovieRepository.findByUser(user)).thenReturn(List.of(wm1, wm2));

        List<WatchedMovieDto> result = movieService.getWatchedMovies(user);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).tmdbId()).isEqualTo(1L);
        assertThat(result.get(0).year()).isEqualTo(2010);
        assertThat(result.get(0).watchedAt()).isEqualTo(watchedAt1);
        assertThat(result.get(1).tmdbId()).isEqualTo(2L);
        assertThat(result.get(1).year()).isNull();
        assertThat(result.get(1).watchedAt()).isEqualTo(watchedAt2);
    }
}
