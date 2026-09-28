package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlType(name = "watchlistItemSoapDto", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class WatchlistItemSoapDto {
    @XmlElement(required = true)
    private Long movieId;

    @XmlElement(required = true)
    private String title;

    private String overview;
    private Double rating;
    private String posterUrl;
    private String genres;
    private String addedAt;

    public WatchlistItemSoapDto() {}

    public WatchlistItemSoapDto(Long movieId, String title, String overview, Double rating,
                                String posterUrl, String genres, String addedAt) {
        this.movieId = movieId;
        this.title = title;
        this.overview = overview;
        this.rating = rating;
        this.posterUrl = posterUrl;
        this.genres = genres;
        this.addedAt = addedAt;
    }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getAddedAt() { return addedAt; }
    public void setAddedAt(String addedAt) { this.addedAt = addedAt; }
}
