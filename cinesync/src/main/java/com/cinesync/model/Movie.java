package com.cinesync.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

        @NotBlank
        @Column(nullable = false, length = 200)
    private String title;

        @Column(columnDefinition = "TEXT")
        private String overview;

        @DecimalMin("0.0")
        @DecimalMax("10.0")
        private Double rating;

        @Column(name = "release_date")
        private LocalDate releaseDate;

        @Column(name = "poster_url", length = 500)
        private String posterUrl;

        @ManyToMany
        @JoinTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
        @OrderBy("name ASC")
        private Set<Genre> genres = new LinkedHashSet<>();

        @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
        private LocalDateTime updatedAt;

    protected Movie() { }

    public Movie(String title) { this.title = title; }

    public Movie(String title, String overview, Double rating, LocalDate releaseDate, String posterUrl) {
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.releaseDate = releaseDate;
        this.posterUrl = posterUrl;
    }

    public void addGenre(Genre genre) { genres.add(genre); }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public Double getRating() { return rating; }
    public LocalDate getReleaseDate() { return releaseDate; }
    public String getPosterUrl() { return posterUrl; }
    public Set<Genre> getGenres() { return genres; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
