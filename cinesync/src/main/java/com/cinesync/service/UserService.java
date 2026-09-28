package com.cinesync.service;

import com.cinesync.dto.RegistrationForm;
import com.cinesync.model.User;
import com.cinesync.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PreferenceService preferenceService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this(userRepository, passwordEncoder, null);
    }

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       PreferenceService preferenceService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.preferenceService = preferenceService;
    }

    @Transactional
    public User register(RegistrationForm form) {
        String username = form.getUsername().trim();
        String email = form.getEmail().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateUserException("That username is already registered.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateUserException("That email is already registered.");
        }

        User user = new User(username, email, passwordEncoder.encode(form.getPassword()), form.getFullName().trim());
        try {
            User saved = userRepository.save(user);
            if (preferenceService != null) {
                preferenceService.createDefaultsFor(saved);
            }
            return saved;
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateUserException("The username or email is already registered.");
        }
    }

    @Transactional(readOnly = true)
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
