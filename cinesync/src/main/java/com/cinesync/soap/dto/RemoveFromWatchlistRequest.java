package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "removeFromWatchlistRequest", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoveFromWatchlistRequest {
    @XmlElement(required = true)
    private String username;

    @XmlElement(required = true)
    private Long movieId;

    public RemoveFromWatchlistRequest() {}

    public RemoveFromWatchlistRequest(String username, Long movieId) {
        this.username = username;
        this.movieId = movieId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
}
