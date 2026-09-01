package com.cinetrack.services;

import com.cinetrack.dto.MovieDetailDto;
import com.cinetrack.dto.MovieSearchResultDto;
import com.cinetrack.dto.tmdb.TmdbCreditsDto;
import com.cinetrack.dto.tmdb.TmdbCrewMemberDto;
import com.cinetrack.dto.tmdb.TmdbMovieDetailsDto;
import com.cinetrack.dto.tmdb.TmdbMovieDetailsWithCreditsDto;
import com.cinetrack.dto.tmdb.TmdbMovieDto;
import com.cinetrack.dto.tmdb.TmdbSearchResponse;
import com.cinetrack.exceptions.MovieNotFoundException;
import com.cinetrack.exceptions.TmdbApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * RestClient is mocked by chaining each link of its fluent API
 * (get() -> uri() -> retrieve() -> onStatus()* -> body()), since there is no
 * direct way to mock the final result with Mockito.
 *
 * 4xx/5xx error scenarios can't actually be triggered through onStatus() on a
 * mocked ResponseSpec (the predicate is never evaluated against a real
 * response). Instead, the outcome of the onStatus handler already having
 * thrown the exception is simulated by making body() throw it directly: from
 * TmdbService's perspective the observable effect is identical.
 */
@ExtendWith(MockitoExtension.class)
class TmdbServiceTest {

    @Mock
    private RestClient restClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private RestClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private TmdbService tmdbService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        tmdbService = new TmdbService(restClient);

        when(restClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }

    // ==================== searchMovies ====================

    @Test
    void searchMovies_success_mapsResultsAndParsesYear() {
        TmdbMovieDto tmdbMovie = new TmdbMovieDto(550L, "Fight Club", "/poster.jpg", "1999-10-15", "overview");
        TmdbSearchResponse response = new TmdbSearchResponse(1, List.of(tmdbMovie), 1, 1);
        when(responseSpec.body(TmdbSearchResponse.class)).thenReturn(response);

        List<MovieSearchResultDto> result = tmdbService.searchMovies("fight club");

        assertThat(result).hasSize(1);
        MovieSearchResultDto dto = result.get(0);
        assertThat(dto.tmdbId()).isEqualTo(550L);
        assertThat(dto.title()).isEqualTo("Fight Club");
        assertThat(dto.posterPath()).isEqualTo("/poster.jpg");
        assertThat(dto.year()).isEqualTo(1999);
        assertThat(dto.overview()).isEqualTo("overview");
    }

    @Test
    void searchMovies_nullResponseBody_returnsEmptyList() {
        when(responseSpec.body(TmdbSearchResponse.class)).thenReturn(null);

        List<MovieSearchResultDto> result = tmdbService.searchMovies("anything");

        assertThat(result).isEmpty();
    }

    @Test
    void searchMovies_nullResultsInResponse_returnsEmptyList() {
        TmdbSearchResponse response = new TmdbSearchResponse(1, null, 1, 0);
        when(responseSpec.body(TmdbSearchResponse.class)).thenReturn(response);

        List<MovieSearchResultDto> result = tmdbService.searchMovies("anything");

        assertThat(result).isEmpty();
    }

    @Test
    void searchMovies_blankOrInvalidReleaseDate_yearIsNullWithoutBreakingMapping() {
        TmdbMovieDto blankDate = new TmdbMovieDto(1L, "No Date", null, "", "overview");
        TmdbMovieDto invalidDate = new TmdbMovieDto(2L, "Bad Date", null, "not-a-date", "overview");
        TmdbSearchResponse response = new TmdbSearchResponse(1, List.of(blankDate, invalidDate), 1, 2);
        when(responseSpec.body(TmdbSearchResponse.class)).thenReturn(response);

        List<MovieSearchResultDto> result = tmdbService.searchMovies("query");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).year()).isNull();
        assertThat(result.get(1).year()).isNull();
    }

    @Test
    void searchMovies_tmdbApiExceptionFromClient_isRethrownAsIs() {
        TmdbApiException original = new TmdbApiException("TMDB client error: 401", HttpStatus.UNAUTHORIZED);
        when(responseSpec.body(TmdbSearchResponse.class)).thenThrow(original);

        assertThatThrownBy(() -> tmdbService.searchMovies("query"))
                .isSameAs(original);
    }

    @Test
    void searchMovies_networkFailure_wrapsAsServiceUnavailable() {
        when(responseSpec.body(TmdbSearchResponse.class))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> tmdbService.searchMovies("query"))
                .isInstanceOf(TmdbApiException.class)
                .satisfies(ex -> assertThat(((TmdbApiException) ex).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void searchMovies_unexpectedFailure_wrapsAsInternalServerError() {
        when(responseSpec.body(TmdbSearchResponse.class)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> tmdbService.searchMovies("query"))
                .isInstanceOf(TmdbApiException.class)
                .satisfies(ex -> assertThat(((TmdbApiException) ex).getStatus())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ==================== getMovieDetails ====================

    @Test
    void getMovieDetails_success_returnsDto() {
        TmdbMovieDetailsDto dto = new TmdbMovieDetailsDto(550L, "Fight Club", "/poster.jpg", "1999-10-15", "overview");
        when(responseSpec.body(TmdbMovieDetailsDto.class)).thenReturn(dto);

        TmdbMovieDetailsDto result = tmdbService.getMovieDetails(550L);

        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getMovieDetails_notFound_rethrowsMovieNotFoundException() {
        MovieNotFoundException original = new MovieNotFoundException("Movie with TMDB ID 999 not found");
        when(responseSpec.body(TmdbMovieDetailsDto.class)).thenThrow(original);

        assertThatThrownBy(() -> tmdbService.getMovieDetails(999L))
                .isSameAs(original);
    }

    @Test
    void getMovieDetails_tmdbApiException_isRethrownAsIs() {
        TmdbApiException original = new TmdbApiException("TMDB server error", HttpStatus.SERVICE_UNAVAILABLE);
        when(responseSpec.body(TmdbMovieDetailsDto.class)).thenThrow(original);

        assertThatThrownBy(() -> tmdbService.getMovieDetails(550L))
                .isSameAs(original);
    }

    @Test
    void getMovieDetails_networkFailure_wrapsAsServiceUnavailable() {
        when(responseSpec.body(TmdbMovieDetailsDto.class))
                .thenThrow(new ResourceAccessException("connection refused"));

        assertThatThrownBy(() -> tmdbService.getMovieDetails(550L))
                .isInstanceOf(TmdbApiException.class)
                .satisfies(ex -> assertThat(((TmdbApiException) ex).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void getMovieDetails_unexpectedFailure_wrapsAsInternalServerError() {
        when(responseSpec.body(TmdbMovieDetailsDto.class)).thenThrow(new RuntimeException("boom"));

        assertThatThrownBy(() -> tmdbService.getMovieDetails(550L))
                .isInstanceOf(TmdbApiException.class)
                .satisfies(ex -> assertThat(((TmdbApiException) ex).getStatus())
                        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    // ==================== getMovieDetail ====================

    /**
     * Director resolution is pure logic (it doesn't depend on the mocked RestClient),
     * so these cases cover that logic directly: one director, multiple directors
     * (the Coens), no crew member with job=="Director", and null credits.
     */

    @Test
    void getMovieDetail_singleDirector_resolvesDirectorName() {
        TmdbCrewMemberDto director = new TmdbCrewMemberDto("David Fincher", "Director");
        TmdbCrewMemberDto writer = new TmdbCrewMemberDto("Jim Uhls", "Screenplay");
        TmdbCreditsDto credits = new TmdbCreditsDto(List.of(director, writer));
        TmdbMovieDetailsWithCreditsDto response = new TmdbMovieDetailsWithCreditsDto(
                550L, "Fight Club", "/poster.jpg", "1999-10-15", "overview", credits);
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenReturn(response);

        MovieDetailDto result = tmdbService.getMovieDetail(550L);

        assertThat(result.tmdbId()).isEqualTo(550L);
        assertThat(result.title()).isEqualTo("Fight Club");
        assertThat(result.year()).isEqualTo(1999);
        assertThat(result.director()).isEqualTo("David Fincher");
    }

    @Test
    void getMovieDetail_multipleDirectors_joinsNamesWithComma() {
        TmdbCrewMemberDto joel = new TmdbCrewMemberDto("Joel Coen", "Director");
        TmdbCrewMemberDto ethan = new TmdbCrewMemberDto("Ethan Coen", "Director");
        TmdbCreditsDto credits = new TmdbCreditsDto(List.of(joel, ethan));
        TmdbMovieDetailsWithCreditsDto response = new TmdbMovieDetailsWithCreditsDto(
                115L, "The Big Lebowski", "/poster.jpg", "1998-03-06", "overview", credits);
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenReturn(response);

        MovieDetailDto result = tmdbService.getMovieDetail(115L);

        assertThat(result.director()).isEqualTo("Joel Coen, Ethan Coen");
    }

    @Test
    void getMovieDetail_noCrewMemberIsDirector_directorIsNull() {
        TmdbCrewMemberDto writer = new TmdbCrewMemberDto("Someone", "Screenplay");
        TmdbCrewMemberDto photography = new TmdbCrewMemberDto("Someone Else", "Director of Photography");
        TmdbCreditsDto credits = new TmdbCreditsDto(List.of(writer, photography));
        TmdbMovieDetailsWithCreditsDto response = new TmdbMovieDetailsWithCreditsDto(
                1L, "No Director Listed", null, null, "overview", credits);
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenReturn(response);

        MovieDetailDto result = tmdbService.getMovieDetail(1L);

        assertThat(result.director()).isNull();
    }

    @Test
    void getMovieDetail_nullCredits_directorIsNull() {
        TmdbMovieDetailsWithCreditsDto response = new TmdbMovieDetailsWithCreditsDto(
                1L, "No Credits", null, null, "overview", null);
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenReturn(response);

        MovieDetailDto result = tmdbService.getMovieDetail(1L);

        assertThat(result.director()).isNull();
    }

    @Test
    void getMovieDetail_nullBody_throwsServiceUnavailable() {
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenReturn(null);

        assertThatThrownBy(() -> tmdbService.getMovieDetail(550L))
                .isInstanceOf(TmdbApiException.class)
                .satisfies(ex -> assertThat(((TmdbApiException) ex).getStatus())
                        .isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
    }

    @Test
    void getMovieDetail_notFound_rethrowsMovieNotFoundException() {
        MovieNotFoundException original = new MovieNotFoundException("Movie with TMDB ID 999 not found");
        when(responseSpec.body(TmdbMovieDetailsWithCreditsDto.class)).thenThrow(original);

        assertThatThrownBy(() -> tmdbService.getMovieDetail(999L))
                .isSameAs(original);
    }
}
