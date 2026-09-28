package com.cinesync.service;

import com.cinesync.model.User;
import com.cinesync.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DatabaseUserDetailsServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final DatabaseUserDetailsService service = new DatabaseUserDetailsService(repository);

    @Test
    void loadsDatabasePasswordAndRole() {
        User user = new User("alice", "alice@example.com", "bcrypt-hash", "Alice");
        when(repository.findByUsername("alice")).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("bcrypt-hash", details.getPassword());
        assertTrue(details.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
        assertTrue(details.isEnabled());
    }

    @Test
    void missingUserFailsAuthenticationLookup() {
        when(repository.findByUsername("missing")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing"));
    }
}
