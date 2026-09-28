package com.cinesync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.CONFIRMED;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<ReservationSeat> seats = new ArrayList<>();

    protected Reservation() { }

    public Reservation(User user, Show show) {
        this.user = user;
        this.show = show;
    }

    public void addSeat(ReservationSeat seat) {
        seats.add(seat);
        seat.setReservation(this);
    }

    public void cancel() {
        status = ReservationStatus.CANCELLED;
        cancelledAt = LocalDateTime.now();
        seats.forEach(ReservationSeat::release);
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Show getShow() { return show; }
    public ReservationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public List<ReservationSeat> getSeats() { return seats; }
}
