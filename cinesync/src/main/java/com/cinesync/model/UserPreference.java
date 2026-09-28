package com.cinesync.model;

import jakarta.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "user_preferences")
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "showtime_preference", nullable = false, length = 20)
    private ShowtimePreference showtimePreference = ShowtimePreference.ANY;

    @ManyToMany
    @JoinTable(name = "preference_genres",
            joinColumns = @JoinColumn(name = "preference_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    @OrderBy("name ASC")
    private Set<Genre> preferredGenres = new LinkedHashSet<>();

    protected UserPreference() { }

    public UserPreference(User user) { this.user = user; }

    public void update(Set<Genre> genres, ShowtimePreference preference) {
        preferredGenres.clear();
        preferredGenres.addAll(genres);
        showtimePreference = preference == null ? ShowtimePreference.ANY : preference;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public ShowtimePreference getShowtimePreference() { return showtimePreference; }
    public Set<Genre> getPreferredGenres() { return preferredGenres; }
}
