package com.cinesync.service;

import com.cinesync.model.*;
import com.cinesync.repository.ReservationRepository;
import com.cinesync.repository.ReservationSeatRepository;
import com.cinesync.repository.ShowRepository;
import com.cinesync.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReservationService {
    private static final String SEAT_PATTERN = "^[A-Z]+[1-9][0-9]*$";

    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSeatRepository reservationSeatRepository;

    public ReservationService(UserRepository userRepository, ShowRepository showRepository,
                              ReservationRepository reservationRepository, ReservationSeatRepository reservationSeatRepository) {
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.reservationRepository = reservationRepository;
        this.reservationSeatRepository = reservationSeatRepository;
    }

    @Transactional
    public Reservation createReservation(String username, Long showId, List<String> requestedSeats) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));
        List<String> seats = normalize(requestedSeats);
        validateSeats(show, seats);
        if (!reservationSeatRepository.findActiveSeatNumbers(showId, seats).isEmpty()) {
            throw new SeatUnavailableException("One or more requested seats are unavailable");
        }

        Reservation reservation = new Reservation(user, show);
        seats.forEach(seat -> reservation.addSeat(new ReservationSeat(show, seat)));
        try {
            reservationRepository.saveAndFlush(reservation);
            return reservation;
        } catch (DataIntegrityViolationException exception) {
            throw new SeatUnavailableException("One or more requested seats are unavailable");
        }
    }

    @Transactional
    public void cancelReservation(String username, Long reservationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, user.getId())
                .orElseThrow(() -> new ReservationNotFoundException("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }
        reservation.cancel();
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getUserReservations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return reservationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    @Transactional(readOnly = true)
    public SeatAvailability getSeatAvailability(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));
        List<String> all = validSeats(show.getTheatre());
        List<String> booked = reservationSeatRepository.findActiveSeatNumbers(showId).stream()
                .sorted().toList();
        Set<String> bookedSet = new HashSet<>(booked);
        List<String> available = all.stream().filter(seat -> !bookedSet.contains(seat)).toList();
        return new SeatAvailability(all, booked, available);
    }

    private List<String> normalize(List<String> requestedSeats) {
        if (requestedSeats == null || requestedSeats.isEmpty()) {
            throw new InvalidSeatException("At least one seat is required");
        }
        List<String> seats = requestedSeats.stream()
                .map(seat -> seat == null ? "" : seat.trim().toUpperCase(Locale.ROOT))
                .toList();
        if (new HashSet<>(seats).size() != seats.size()) {
            throw new InvalidSeatException("Duplicate seats are not allowed");
        }
        return seats;
    }

    private void validateSeats(Show show, List<String> seats) {
        Set<String> valid = new HashSet<>(validSeats(show.getTheatre()));
        for (String seat : seats) {
            if (!seat.matches(SEAT_PATTERN) || !valid.contains(seat)) {
                throw new InvalidSeatException("Invalid seat: " + seat);
            }
        }
    }

    private List<String> validSeats(Theatre theatre) {
        List<String> seats = new ArrayList<>();
        for (int row = 0; row < theatre.getRows(); row++) {
            String rowName = rowName(row);
            for (int number = 1; number <= theatre.getSeatsPerRow(); number++) {
                seats.add(rowName + number);
            }
        }
        return seats;
    }

    private String rowName(int index) {
        StringBuilder result = new StringBuilder();
        int value = index;
        do {
            result.insert(0, (char) ('A' + value % 26));
            value = value / 26 - 1;
        } while (value >= 0);
        return result.toString();
    }
}
