package com.cinesync.service;

import com.cinesync.dto.PreferenceForm;
import com.cinesync.dto.PreferenceView;
import com.cinesync.model.*;
import com.cinesync.repository.GenreRepository;
import com.cinesync.repository.UserPreferenceRepository;
import com.cinesync.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PreferenceServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final UserPreferenceRepository preferences = mock(UserPreferenceRepository.class);
    private final GenreRepository genres = mock(GenreRepository.class);
    private final PreferenceService service = new PreferenceService(users, preferences, genres);

    @Test
    void createsAnyDefaultsForNewUser() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        when(preferences.findByUserUsername("alice")).thenReturn(Optional.empty());
        when(preferences.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserPreference preference = service.createDefaultsFor(user);

        assertEquals(ShowtimePreference.ANY, preference.getShowtimePreference());
        assertTrue(preference.getPreferredGenres().isEmpty());
        verify(preferences).save(any(UserPreference.class));
    }

    @Test
    void readsAndUpdatesOnlyTheAuthenticatedUsername() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        Genre drama = new Genre("Drama");
        UserPreference preference = new UserPreference(user);
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(preferences.findByUserUsername("alice")).thenReturn(Optional.of(preference));
        when(genres.findAllById(java.util.Set.of(1L))).thenReturn(List.of(drama));
        when(preferences.save(preference)).thenReturn(preference);

        PreferenceForm form = new PreferenceForm();
        form.setGenreIds(java.util.Set.of(1L));
        form.setShowtimePreference(ShowtimePreference.EVENING);
        PreferenceView updated = service.update("alice", form);

        assertEquals(ShowtimePreference.EVENING, updated.showtimePreference());
        assertEquals(List.of("Drama"), updated.preferredGenres());
        verify(preferences).findByUserUsername("alice");
        verify(preferences, never()).findByUserUsername("bob");
    }
}
