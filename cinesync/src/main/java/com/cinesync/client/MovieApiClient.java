package com.cinesync.client;

import com.cinesync.client.dto.TmdbMovieDto;
import com.cinesync.client.dto.TmdbSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * HTTP client for the TMDB (The Movie Database) API.
 *
 * Configuration (environment variables / application.properties):
 *   TMDB_API_KEY  – Bearer token API key (v4 auth) or API key param (v3)
 *   tmdb.base-url – defaults to https://api.themoviedb.org/3
 *   tmdb.image-base-url – defaults to https://image.tmdb.org/t/p/w500
 *
 * This client NEVER crashes the application if the external API is unavailable.
 * All methods return empty optionals or empty lists on failure.
 */
@Component
public class MovieApiClient {

    private static final Logger log = LoggerFactory.getLogger(MovieApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String imageBaseUrl;
    private final String apiKey;

    public MovieApiClient(
            RestTemplate externalApiRestTemplate,
            @Value("${tmdb.base-url:https://api.themoviedb.org/3}") String baseUrl,
            @Value("${tmdb.image-base-url:https://image.tmdb.org/t/p/w500}") String imageBaseUrl,
            @Value("${TMDB_API_KEY:}") String apiKey) {
        this.restTemplate = externalApiRestTemplate;
        this.baseUrl = baseUrl;
        this.imageBaseUrl = imageBaseUrl;
        this.apiKey = apiKey;
    }

    /**
     * Search for movies by title query.
     *
     * @param query the movie title search string
     * @return list of matching movies, or empty list on failure
     */
    public List<TmdbMovieDto> searchMovies(String query) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("TMDB_API_KEY is not configured. Movie search is unavailable.");
            return Collections.emptyList();
        }
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/search/movie")
                    .queryParam("api_key", apiKey)
                    .queryParam("query", query)
                    .queryParam("include_adult", false)
                    .build()
                    .toUriString();

            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
            if (response == null || response.getResults() == null) {
                return Collections.emptyList();
            }
            List<TmdbMovieDto> results = response.getResults();
            results.forEach(movie -> {
                if (movie.getPosterPath() != null) {
                    movie.setPosterUrl(imageBaseUrl + movie.getPosterPath());
                }
            });
            log.debug("TMDB search for '{}' returned {} results", query, results.size());
            return results;
        } catch (ResourceAccessException e) {
            log.warn("TMDB API is unreachable (connection timeout or refused): {}", e.getMessage());
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.warn("TMDB API returned an error response for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Discover popular/trending movies (no query required).
     *
     * @return list of popular movies, or empty list on failure
     */
    public List<TmdbMovieDto> discoverPopular() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("TMDB_API_KEY is not configured. Movie discovery is unavailable.");
            return Collections.emptyList();
        }
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/movie/popular")
                    .queryParam("api_key", apiKey)
                    .build()
                    .toUriString();

            TmdbSearchResponse response = restTemplate.getForObject(url, TmdbSearchResponse.class);
            if (response == null || response.getResults() == null) {
                return Collections.emptyList();
            }
            List<TmdbMovieDto> results = response.getResults();
            results.forEach(movie -> {
                if (movie.getPosterPath() != null) {
                    movie.setPosterUrl(imageBaseUrl + movie.getPosterPath());
                }
            });
            log.debug("TMDB popular movies returned {} results", results.size());
            return results;
        } catch (ResourceAccessException e) {
            log.warn("TMDB API is unreachable: {}", e.getMessage());
            return Collections.emptyList();
        } catch (RestClientException e) {
            log.warn("TMDB API returned an error response for popular movies: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * @return true if a TMDB API key is configured
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
