package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "updateUserPreferencesRequest", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateUserPreferencesRequest {
    @XmlElement(required = true)
    private String username;

    private String genres;

    @XmlElement(name = "genreIds")
    private List<Long> genreIds = new ArrayList<>();

    private String preferredShowtime;

    public UpdateUserPreferencesRequest() {}

    public UpdateUserPreferencesRequest(String username, String genres, String preferredShowtime) {
        this.username = username;
        this.genres = genres;
        this.preferredShowtime = preferredShowtime;
    }

    public UpdateUserPreferencesRequest(String username, List<Long> genreIds, String preferredShowtime) {
        this.username = username;
        this.genreIds = genreIds;
        this.preferredShowtime = preferredShowtime;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public List<Long> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Long> genreIds) { this.genreIds = genreIds; }
    public String getPreferredShowtime() { return preferredShowtime; }
    public void setPreferredShowtime(String preferredShowtime) { this.preferredShowtime = preferredShowtime; }
}
