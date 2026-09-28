package com.cinesync.service;

import com.cinesync.model.Movie;
import com.cinesync.model.User;
import com.cinesync.model.WatchlistItem;
import com.cinesync.repository.MovieRepository;
import com.cinesync.repository.UserRepository;
import com.cinesync.repository.WatchlistRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WatchlistServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final MovieRepository movies = mock(MovieRepository.class);
    private final WatchlistRepository watchlists = mock(WatchlistRepository.class);
    private final MovieService movieService = mock(MovieService.class);
    private final WatchlistService service = new WatchlistService(users, movies, watchlists, movieService);

    @Test
    void addsMovieForAuthenticatedUser() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        Movie movie = new Movie("Example");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(movies.findById(1L)).thenReturn(Optional.of(movie));
        when(watchlists.existsByUserUsernameAndMovieId("alice", 1L)).thenReturn(false);
        when(watchlists.save(any(WatchlistItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertNotNull(service.addMovieToWatchlist("alice", 1L));
        verify(watchlists).save(any(WatchlistItem.class));
    }

    @Test
    void rejectsDuplicateMovie() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(watchlists.existsByUserUsernameAndMovieId("alice", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.addMovieToWatchlist("alice", 1L));
        verifyNoInteractions(movies);
    }

    @Test
    void removesOnlyCurrentUsersItem() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(watchlists.existsByUserUsernameAndMovieId("alice", 1L)).thenReturn(true);

        service.removeMovieFromWatchlist("alice", 1L);

        verify(watchlists).deleteByUserUsernameAndMovieId("alice", 1L);
        verify(watchlists, never()).deleteByUserUsernameAndMovieId("bob", 1L);
    }

    @Test
    void missingItemIsHandledCleanly() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(watchlists.existsByUserUsernameAndMovieId("alice", 1L)).thenReturn(false);

        assertThrows(WatchlistItemNotFoundException.class,
                () -> service.removeMovieFromWatchlist("alice", 1L));
        verify(watchlists, never()).deleteByUserUsernameAndMovieId(anyString(), anyLong());
    }
}
