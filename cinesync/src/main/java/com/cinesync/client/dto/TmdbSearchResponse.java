package com.cinesync.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * External DTO representing the paginated search/discover response from TMDB API.
 * This is NOT a JPA entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbSearchResponse {

    @JsonProperty("results")
    private List<TmdbMovieDto> results;

    @JsonProperty("total_results")
    private int totalResults;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("page")
    private int page;

    public List<TmdbMovieDto> getResults() { return results; }
    public int getTotalResults() { return totalResults; }
    public int getTotalPages() { return totalPages; }
    public int getPage() { return page; }
}
