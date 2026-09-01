package com.cinetrack.controller;

import com.cinetrack.dto.MovieDetailDto;
import com.cinetrack.exceptions.MovieNotFoundException;
import com.cinetrack.exceptions.TmdbApiException;
import com.cinetrack.services.TmdbService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ViewController {

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
            ModelAndView mav = new ModelAndView("pages/movie-detail-error", HttpStatus.NOT_FOUND);
            mav.addObject("title", "Película no encontrada - CineTrack");
            mav.addObject("message", "No hemos encontrado esta película.");
            return mav;
        } catch (TmdbApiException e) {
            ModelAndView mav = new ModelAndView("pages/movie-detail-error", HttpStatus.SERVICE_UNAVAILABLE);
            mav.addObject("title", "Error - CineTrack");
            mav.addObject("message", "No se pudo conectar con el servicio de películas. Inténtalo de nuevo más tarde.");
            return mav;
        }
    }
}