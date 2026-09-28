package com.cinesync.service;

import com.cinesync.dto.RegistrationForm;
import com.cinesync.model.User;
import com.cinesync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final PreferenceService preferences = mock(PreferenceService.class);
    private final UserService service = new UserService(repository, encoder, preferences);

    @Test
    void registrationHashesPasswordAndDefaultsToEnabledUserRole() {
        RegistrationForm form = form("alice", "Alice@example.com", "correct horse battery staple");
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User user = service.register(form);

        assertEquals("alice", user.getUsername());
        assertEquals("alice@example.com", user.getEmail());
        assertNotEquals(form.getPassword(), user.getPassword());
        assertTrue(encoder.matches(form.getPassword(), user.getPassword()));
        assertEquals("USER", user.getRole());
        assertTrue(user.isEnabled());
        verify(preferences).createDefaultsFor(user);
    }

    @Test
    void duplicateUsernameIsRejectedBeforeSaving() {
        RegistrationForm form = form("alice", "alice@example.com", "correct horse battery staple");
        when(repository.existsByUsername("alice")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> service.register(form));
        verify(repository, never()).save(any());
    }

    @Test
    void duplicateEmailIsRejectedBeforeSaving() {
        RegistrationForm form = form("alice", "alice@example.com", "correct horse battery staple");
        when(repository.existsByEmailIgnoreCase("alice@example.com")).thenReturn(true);

        assertThrows(DuplicateUserException.class, () -> service.register(form));
        verify(repository, never()).save(any());
    }

    private RegistrationForm form(String username, String email, String password) {
        RegistrationForm form = new RegistrationForm();
        form.setUsername(username);
        form.setEmail(email);
        form.setPassword(password);
        form.setFullName("Alice Example");
        return form;
    }
}
