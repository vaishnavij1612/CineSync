package com.cinesync.controller;

import com.cinesync.service.WatchlistItemNotFoundException;
import com.cinesync.service.WatchlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/watchlist")
public class WatchlistController {
    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) { this.watchlistService = watchlistService; }

    @GetMapping
    public String watchlist(Authentication authentication, Model model) {
        model.addAttribute("watchlist", watchlistService.getCurrentUserWatchlist(authentication.getName()));
        return "watchlist";
    }

    @PostMapping("/add/{movieId}")
    public String add(@PathVariable Long movieId, Authentication authentication, Model model) {
        try {
            watchlistService.addMovieToWatchlist(authentication.getName(), movieId);
            return "redirect:/watchlist?updated";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("watchlistError", exception.getMessage());
            model.addAttribute("watchlist", watchlistService.getCurrentUserWatchlist(authentication.getName()));
            return "watchlist";
        }
    }

    @PostMapping("/remove/{movieId}")
    public String remove(@PathVariable Long movieId, Authentication authentication, Model model) {
        try {
            watchlistService.removeMovieFromWatchlist(authentication.getName(), movieId);
            return "redirect:/watchlist?updated";
        } catch (WatchlistItemNotFoundException exception) {
            model.addAttribute("watchlistError", exception.getMessage());
            model.addAttribute("watchlist", watchlistService.getCurrentUserWatchlist(authentication.getName()));
            return "watchlist";
        }
    }
}
