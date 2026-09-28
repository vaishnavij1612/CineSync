package com.cinesync.controller;

import com.cinesync.service.MovieService;
import com.cinesync.service.WatchlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MovieController {
    private final MovieService movieService;
    private final WatchlistService watchlistService;

    public MovieController(MovieService movieService, WatchlistService watchlistService) {
        this.movieService = movieService;
        this.watchlistService = watchlistService;
    }

    @GetMapping("/movies")
    public String movies(Authentication authentication, Model model) {
        model.addAttribute("movies", movieService.findAll());
        model.addAttribute("watchlistedMovieIds", watchlistService.getMovieIds(authentication.getName()));
        return "movies";
    }
}
