package com.cinesync.service;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(String message) { super(message); }
}
