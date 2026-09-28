package com.cinesync.service;

import com.cinesync.client.WeatherApiClient;
import com.cinesync.dto.PreferenceView;
import com.cinesync.dto.ShowView;
import com.cinesync.recommendation.RecommendationEngine;
import com.cinesync.recommendation.RecommendationResult;
import com.cinesync.recommendation.WeatherContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Orchestrates the recommendation pipeline for the authenticated user.
 *
 * Responsibilities:
 *   1. Retrieve preferences via PreferenceService (never direct repo access)
 *   2. Load upcoming shows from ShowService
 *   3. Fetch weather for user coordinates via WeatherApiClient
 *   4. Score each show using RecommendationEngine
 *   5. Sort by descending experienceScore
 *   6. Return ranked RecommendationResult list
 *
 * This service does NOT identify the user – the controller passes the
 * authenticated Spring Security principal's username.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final PreferenceService preferenceService;
    private final ShowService showService;
    private final WeatherApiClient weatherApiClient;
    private final RecommendationEngine engine;

    public RecommendationService(
            PreferenceService preferenceService,
            ShowService showService,
            WeatherApiClient weatherApiClient,
            RecommendationEngine engine) {
        this.preferenceService = preferenceService;
        this.showService = showService;
        this.weatherApiClient = weatherApiClient;
        this.engine = engine;
    }

    /**
     * Returns a ranked list of recommendations for the authenticated user.
     *
     * @param username  authenticated Spring Security principal name
     * @param userLat   user's current latitude
     * @param userLon   user's current longitude
     * @return sorted list of RecommendationResult, best first
     */
    public List<RecommendationResult> recommend(String username, double userLat, double userLon) {
        // Step 1: Load user preferences (via service layer only)
        PreferenceView prefs = preferenceService.getCurrentPreferences(username);
        Set<String> preferredGenres = new LinkedHashSet<>(prefs.preferredGenres());
        var showtimePref = prefs.showtimePreference();

        log.debug("Recommending for user='{}' genres={} showtime={} at ({},{})",
                username, preferredGenres, showtimePref, userLat, userLon);

        // Step 2: Fetch weather for user coordinates with graceful fallback
        Optional<WeatherContext> weatherOpt = weatherApiClient.getCurrentWeather(userLat, userLon);
        WeatherContext weather = weatherOpt.orElseGet(() -> {
            log.warn("Weather API unavailable for user='{}'. Using fallback weather context.", username);
            return WeatherContext.fallback();
        });

        // Step 3: Load all upcoming shows
        List<ShowView> shows = showService.findUpcoming();
        if (shows.isEmpty()) {
            log.debug("No upcoming shows found for recommendation.");
            return List.of();
        }

        // Step 4: Score each show and sort by descending experience score
        return shows.stream()
                .map(show -> engine.score(show, userLat, userLon, preferredGenres, showtimePref, weather))
                .sorted(Comparator.comparingDouble(RecommendationResult::experienceScore).reversed())
                .toList();
    }
}
