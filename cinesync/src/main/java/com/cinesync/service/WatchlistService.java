package com.cinesync.service;

import com.cinesync.dto.MovieView;
import com.cinesync.dto.WatchlistView;
import com.cinesync.model.Movie;
import com.cinesync.model.User;
import com.cinesync.model.WatchlistItem;
import com.cinesync.repository.MovieRepository;
import com.cinesync.repository.UserRepository;
import com.cinesync.repository.WatchlistRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WatchlistService {
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final WatchlistRepository watchlistRepository;
    private final MovieService movieService;

    public WatchlistService(UserRepository userRepository, MovieRepository movieRepository,
                            WatchlistRepository watchlistRepository, MovieService movieService) {
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.watchlistRepository = watchlistRepository;
        this.movieService = movieService;
    }

    @Transactional
    public WatchlistItem addMovieToWatchlist(String username, Long movieId) {
        User user = user(username);
        if (watchlistRepository.existsByUserUsernameAndMovieId(username, movieId)) {
            throw new IllegalArgumentException("Movie is already in your watchlist");
        }
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new IllegalArgumentException("Movie not found"));
        try {
            return watchlistRepository.save(new WatchlistItem(user, movie));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Movie is already in your watchlist", exception);
        }
    }

    @Transactional
    public void removeMovieFromWatchlist(String username, Long movieId) {
        if (!watchlistRepository.existsByUserUsernameAndMovieId(username, movieId)) {
            throw new WatchlistItemNotFoundException("Movie is not in your watchlist");
        }
        watchlistRepository.deleteByUserUsernameAndMovieId(username, movieId);
    }

    @Transactional(readOnly = true)
    public List<WatchlistView> getCurrentUserWatchlist(String username) {
        user(username);
        return watchlistRepository.findByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(item -> new WatchlistView(movieService.toView(item.getMovie()), item.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean isMovieInWatchlist(String username, Long movieId) {
        user(username);
        return watchlistRepository.existsByUserUsernameAndMovieId(username, movieId);
    }

    @Transactional(readOnly = true)
    public List<Long> getMovieIds(String username) {
        return watchlistRepository.findByUserUsernameOrderByCreatedAtDesc(username).stream()
                .map(item -> item.getMovie().getId()).toList();
    }

    private User user(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
