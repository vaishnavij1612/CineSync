package com.cinesync.controller;

import com.cinesync.model.Reservation;
import com.cinesync.service.InvalidSeatException;
import com.cinesync.service.ReservationService;
import com.cinesync.service.SeatUnavailableException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/shows/{showId}/seats")
    public String seats(@PathVariable Long showId, Model model) {
        model.addAttribute("showId", showId);
        model.addAttribute("availability", reservationService.getSeatAvailability(showId));
        return "reservation-seats";
    }

    @PostMapping
    public String reserve(@RequestParam Long showId, @RequestParam String seats,
                          Authentication authentication, Model model) {
        try {
            Reservation reservation = reservationService.createReservation(
                    authentication.getName(), showId, Arrays.asList(seats.split(",")));
            model.addAttribute("reservation", reservation);
            return "reservation-success";
        } catch (InvalidSeatException | SeatUnavailableException exception) {
            model.addAttribute("showId", showId);
            model.addAttribute("availability", reservationService.getSeatAvailability(showId));
            model.addAttribute("error", exception.getMessage());
            return "reservation-seats";
        }
    }
}
