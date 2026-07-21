package com.cinetrack.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

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
}