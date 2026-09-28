package com.cinesync.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * External DTO representing a movie result from the TMDB API.
 * This is NOT a JPA entity. It is used only within the client layer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDto {

    @JsonProperty("id")
    private Long externalId;

    @JsonProperty("title")
    private String title;

    @JsonProperty("overview")
    private String overview;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonProperty("release_date")
    private String releaseDate;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    // Base URL prepended by the client
    private String posterUrl;

    public Long getExternalId() { return externalId; }
    public String getTitle() { return title; }
    public String getOverview() { return overview; }
    public Double getVoteAverage() { return voteAverage; }
    public String getReleaseDate() { return releaseDate; }
    public String getPosterPath() { return posterPath; }
    public List<Integer> getGenreIds() { return genreIds; }
    public String getPosterUrl() { return posterUrl; }

    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
}
