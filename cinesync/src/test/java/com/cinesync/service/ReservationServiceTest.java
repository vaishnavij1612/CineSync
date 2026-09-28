package com.cinesync.service;

import com.cinesync.model.*;
import com.cinesync.repository.ReservationRepository;
import com.cinesync.repository.ReservationSeatRepository;
import com.cinesync.repository.ShowRepository;
import com.cinesync.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final ShowRepository shows = mock(ShowRepository.class);
    private final ReservationRepository reservations = mock(ReservationRepository.class);
    private final ReservationSeatRepository seats = mock(ReservationSeatRepository.class);
    private final ReservationService service = new ReservationService(users, shows, reservations, seats);

    @Test
    void rejectsDuplicateSeatsBeforePersistence() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        Show show = new Show(new Movie("Example"), new Theatre("Central", 4, 5), java.time.LocalDateTime.now());
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(shows.findById(1L)).thenReturn(Optional.of(show));
        assertThrows(InvalidSeatException.class,
                () -> service.createReservation("alice", 1L, List.of("a1", "A1")));
        verifyNoInteractions(reservations, seats);
    }

    @Test
    void rejectsSeatOutsideTheatreLayout() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        Theatre theatre = new Theatre("Central", 4, 5);
        Movie movie = new Movie("Example");
        Show show = new Show(movie, theatre, java.time.LocalDateTime.now());
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(shows.findById(1L)).thenReturn(Optional.of(show));
        assertThrows(InvalidSeatException.class,
                () -> service.createReservation("alice", 1L, List.of("E1")));
        verify(reservations, never()).saveAndFlush(any());
    }

    @Test
    void rejectsAlreadyBookedSeat() {
        User user = new User("alice", "alice@example.com", "hash", "Alice");
        Theatre theatre = new Theatre("Central", 4, 5);
        Show show = new Show(new Movie("Example"), theatre, java.time.LocalDateTime.now());
        when(users.findByUsername("alice")).thenReturn(Optional.of(user));
        when(shows.findById(1L)).thenReturn(Optional.of(show));
        when(seats.findActiveSeatNumbers(1L, List.of("A1"))).thenReturn(List.of("A1"));
        assertThrows(SeatUnavailableException.class,
                () -> service.createReservation("alice", 1L, List.of("A1")));
        verify(reservations, never()).saveAndFlush(any());
    }
}
