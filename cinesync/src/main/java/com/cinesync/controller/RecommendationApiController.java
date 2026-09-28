package com.cinesync.controller;

import com.cinesync.client.MovieApiClient;
import com.cinesync.dto.MovieDiscoveryDto;
import com.cinesync.dto.RecommendationResponse;
import com.cinesync.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST API controller for recommendations and movie discovery.
 *
 * Routes:
 *   GET /api/recommendations?lat={lat}&lon={lon}
 *     – Returns personalised recommendations for the authenticated user.
 *     – User identity from Spring Security principal only (never browser-supplied).
 *
 *   GET /api/discover/movies?query={query}
 *     – Searches external TMDB API for movies by title.
 *     – Returns empty list (not 500) when TMDB is unavailable.
 *
 * Security: all /api/recommendations/** routes are authenticated in SecurityConfig.
 * /api/discover/** is also authenticated to prevent abuse of external API quotas.
 */
@RestController
@RequestMapping("/api")
public class RecommendationApiController {

    private final RecommendationService recommendationService;
    private final MovieApiClient movieApiClient;

    public RecommendationApiController(
            RecommendationService recommendationService,
            MovieApiClient movieApiClient) {
        this.recommendationService = recommendationService;
        this.movieApiClient = movieApiClient;
    }

    /**
     * Returns personalised movie/show recommendations for the authenticated user.
     *
     * @param lat            user's current latitude (required)
     * @param lon            user's current longitude (required)
     * @param authentication injected by Spring Security – never trust a browser param for user identity
     * @return list of RecommendationResponse sorted by descending experience score
     */
    @GetMapping("/recommendations")
    public ResponseEntity<?> getRecommendations(
            @RequestParam double lat,
            @RequestParam double lon,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }

        String username = authentication.getName();
        try {
            List<RecommendationResponse> results = recommendationService
                    .recommend(username, lat, lon)
                    .stream()
                    .map(RecommendationResponse::from)
                    .toList();
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Recommendation service error: " + e.getMessage()));
        }
    }

    /**
     * Discovers movies from the external TMDB API.
     * Returns an empty list (not 500) if TMDB is unavailable or not configured.
     *
     * @param query optional search query; if blank, returns popular movies
     * @return list of MovieDiscoveryDto
     */
    @GetMapping("/discover/movies")
    public ResponseEntity<?> discoverMovies(
            @RequestParam(required = false, defaultValue = "") String query) {

        try {
            List<MovieDiscoveryDto> results;
            if (query.isBlank()) {
                results = movieApiClient.discoverPopular()
                        .stream().map(MovieDiscoveryDto::from).toList();
            } else {
                results = movieApiClient.searchMovies(query)
                        .stream().map(MovieDiscoveryDto::from).toList();
            }

            if (results.isEmpty() && !movieApiClient.isConfigured()) {
                return ResponseEntity.ok(Map.of(
                        "results", List.of(),
                        "message", "Movie discovery is not available. Set TMDB_API_KEY to enable.",
                        "configured", false
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "results", results,
                    "count", results.size(),
                    "query", query,
                    "configured", movieApiClient.isConfigured()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Movie discovery service error: " + e.getMessage()));
        }
    }
}
