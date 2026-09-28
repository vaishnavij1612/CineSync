package com.cinesync.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_items", uniqueConstraints = @UniqueConstraint(name = "uq_watchlist_user_movie", columnNames = {"user_id", "movie_id"}))
public class WatchlistItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected WatchlistItem() { }

    public WatchlistItem(User user, Movie movie) {
        this.user = user;
        this.movie = movie;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Movie getMovie() { return movie; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
