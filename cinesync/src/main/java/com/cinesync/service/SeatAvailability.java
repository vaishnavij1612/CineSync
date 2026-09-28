package com.cinesync.service;

import java.util.List;

public record SeatAvailability(List<String> allSeats, List<String> bookedSeats, List<String> availableSeats) { }
