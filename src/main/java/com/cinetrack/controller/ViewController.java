package com.cinetrack.controller;

import com.cinetrack.dto.MovieDetailDto;
import com.cinetrack.exceptions.MovieNotFoundException;
import com.cinetrack.exceptions.TmdbApiException;
import com.cinetrack.services.TmdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

    private static final Logger logger = LoggerFactory.getLogger(ViewController.class);

    private final TmdbService tmdbService;

    public ViewController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "CineTrack - Inicio");
        return "pages/index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("title", "Login - CineTrack");
        return "pages/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("title", "Registro - CineTrack");
        return "pages/register";
    }

    @GetMapping("/search")
    public String search(Model model) {
        model.addAttribute("title", "Buscar Películas - CineTrack");
        return "pages/search";
    }

    @GetMapping("/watched")
    public String watched(Model model) {
        model.addAttribute("title", "Mis Vistas - CineTrack");
        return "pages/watched";
    }

    /**
     * Returns ModelAndView (not String) so that error cases can set the HTTP
     * status explicitly: a plain "return viewName" always renders with 200,
     * even for the not-found/service-unavailable views.
     */
    @GetMapping("/movie/{tmdbId}")
    public ModelAndView movieDetail(@PathVariable Long tmdbId) {
        try {
            MovieDetailDto movie = tmdbService.getMovieDetail(tmdbId);
            ModelAndView mav = new ModelAndView("pages/movie-detail");
            mav.addObject("title", movie.title() + " - CineTrack");
            mav.addObject("movie", movie);
            return mav;
        } catch (MovieNotFoundException e) {
            return movieErrorView(HttpStatus.NOT_FOUND, "Película no encontrada - CineTrack",
                    "No hemos encontrado esta película.");
        } catch (TmdbApiException e) {
            logger.warn("TMDB error rendering movie detail for tmdbId {}: {} - {}",
                    tmdbId, e.getStatus(), e.getMessage());
            return movieErrorView(HttpStatus.SERVICE_UNAVAILABLE, "Error - CineTrack",
                    "No se pudo conectar con el servicio de películas. Inténtalo de nuevo más tarde.");
        }
    }

    /**
     * A non-numeric tmdbId (e.g. /movie/abc) fails path variable conversion before
     * movieDetail() ever runs, so its try/catch never gets a chance. A handler local
     * to this controller (not the REST GlobalExceptionHandler, which would answer
     * with JSON here) keeps this on the same "view controller renders its own error
     * view" contract as the rest of this route.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ModelAndView handleInvalidTmdbId() {
        return movieErrorView(HttpStatus.NOT_FOUND, "Película no encontrada - CineTrack",
                "No hemos encontrado esta película.");
    }

    private ModelAndView movieErrorView(HttpStatus status, String title, String message) {
        ModelAndView mav = new ModelAndView("pages/movie-detail-error", status);
        mav.addObject("title", title);
        mav.addObject("message", message);
        return mav;
    }
}