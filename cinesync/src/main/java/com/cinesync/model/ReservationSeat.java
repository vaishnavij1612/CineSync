package com.cinesync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_seats")
public class ReservationSeat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Column(name = "seat_number", nullable = false, length = 10)
    private String seatNumber;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    protected ReservationSeat() { }

    public ReservationSeat(Show show, String seatNumber) {
        this.show = show;
        this.seatNumber = seatNumber;
    }

    void setReservation(Reservation reservation) { this.reservation = reservation; }
    public void release() { releasedAt = LocalDateTime.now(); }
    public Long getId() { return id; }
    public Reservation getReservation() { return reservation; }
    public Show getShow() { return show; }
    public String getSeatNumber() { return seatNumber; }
    public LocalDateTime getReleasedAt() { return releasedAt; }
}
