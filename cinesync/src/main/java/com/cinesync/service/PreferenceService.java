package com.cinesync.service;

import com.cinesync.dto.PreferenceForm;
import com.cinesync.dto.PreferenceView;
import com.cinesync.model.Genre;
import com.cinesync.model.User;
import com.cinesync.model.UserPreference;
import com.cinesync.repository.GenreRepository;
import com.cinesync.repository.UserPreferenceRepository;
import com.cinesync.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PreferenceService {
    private final UserRepository userRepository;
    private final UserPreferenceRepository preferenceRepository;
    private final GenreRepository genreRepository;

    public PreferenceService(UserRepository userRepository, UserPreferenceRepository preferenceRepository,
                             GenreRepository genreRepository) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.genreRepository = genreRepository;
    }

    @Transactional
    public UserPreference createDefaultsFor(User user) {
        return preferenceRepository.findByUserUsername(user.getUsername())
                .orElseGet(() -> preferenceRepository.save(new UserPreference(user)));
    }

    @Transactional
    public PreferenceView getCurrentPreferences(String username) {
        UserPreference preference = preferenceFor(username);
        return toView(preference);
    }

    @Transactional
    public PreferenceView update(String username, PreferenceForm form) {
        UserPreference preference = preferenceFor(username);
        Set<Long> ids = form.getGenreIds() == null ? Set.of() : form.getGenreIds();
        List<Genre> genres = genreRepository.findAllById(ids);
        if (genres.size() != ids.size()) {
            throw new IllegalArgumentException("One or more selected genres do not exist");
        }
        preference.update(new LinkedHashSet<>(genres), form.getShowtimePreference());
        return toView(preferenceRepository.save(preference));
    }

    private UserPreference preferenceFor(String username) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return preferenceRepository.findByUserUsername(username)
                .orElseGet(() -> createDefaultsFor(userRepository.findByUsername(username).orElseThrow()));
    }

    private PreferenceView toView(UserPreference preference) {
        return new PreferenceView(preference.getPreferredGenres().stream().map(Genre::getId).toList(),
            preference.getPreferredGenres().stream().map(Genre::getName).toList(),
                preference.getShowtimePreference());
    }
}
