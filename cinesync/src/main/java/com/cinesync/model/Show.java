package com.cinesync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "shows")
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @Column(nullable = false)
    private LocalDateTime showtime;

    @Column(nullable = false, length = 20)
    private String status = "SCHEDULED";

    @Column(name = "seat_capacity", nullable = false)
    private int seatCapacity;

    protected Show() { }

    public Show(Movie movie, Theatre theatre, LocalDateTime showtime) {
        this.movie = movie;
        this.theatre = theatre;
        this.showtime = showtime;
        this.seatCapacity = theatre.getRows() * theatre.getSeatsPerRow();
    }

    public Long getId() { return id; }
    public Movie getMovie() { return movie; }
    public Theatre getTheatre() { return theatre; }
    public LocalDateTime getShowtime() { return showtime; }
    public String getStatus() { return status; }
    public int getSeatCapacity() { return seatCapacity; }
}
