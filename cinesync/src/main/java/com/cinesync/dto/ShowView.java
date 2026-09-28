package com.cinesync.dto;

import java.time.LocalDateTime;

public record ShowView(Long id, MovieView movie, TheatreView theatre,
                       LocalDateTime showtime, String status, int seatCapacity,
                       long bookedSeats) { }
