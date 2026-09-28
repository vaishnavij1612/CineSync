package com.cinesync.recommendation;

import com.cinesync.dto.MovieView;
import com.cinesync.dto.ShowView;
import com.cinesync.dto.TheatreView;
import com.cinesync.model.ShowtimePreference;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Deterministic recommendation engine.
 *
 * ALL scores are 100% deterministic — no randomness, no AI/ML.
 * Given identical inputs, this engine always produces identical outputs.
 *
 * ══════════════════════════════════════════════════════════════
 * WEIGHT TABLE
 * ══════════════════════════════════════════════════════════════
 * Component          Weight    Raw score range
 * ─────────────────────────────────────────
 * Rating              25%      0 – 100
 * Genre Match         25%      0 – 100
 * Theatre Distance    20%      0 – 100
 * Weather Suitability 15%      0 – 100
 * Showtime Preference 15%      0 – 100
 * ─────────────────────────────────────────
 * TOTAL              100%
 *
 * experienceScore = ratingScore*0.25 + genreScore*0.25 + distanceScore*0.20
 *                 + weatherScore*0.15 + showtimeScore*0.15
 *
 * ══════════════════════════════════════════════════════════════
 * SCORING RULES
 * ══════════════════════════════════════════════════════════════
 *
 * RATING SCORE  (0–100)
 *   TMDB ratings are on a 0–10 scale.
 *   score = (rating / 10.0) * 100
 *   If rating is null → score = 50 (neutral).
 *
 * GENRE SCORE  (0–100)
 *   matchCount = number of movie genres that are also in user preferred genres
 *   totalPreferred = number of user preferred genres
 *   If user has no genre preferences → score = 75 (broad interest, slight bonus)
 *   If movie has no genres → score = 50 (neutral)
 *   score = (matchCount / max(totalPreferred, movieGenreCount)) * 100
 *   Capped at 100.
 *
 * DISTANCE SCORE  (0–100) via Haversine
 *   Uses thresholds:
 *     ≤ 2 km   → 100
 *     ≤ 5 km   → 85
 *     ≤ 10 km  → 65
 *     ≤ 20 km  → 45
 *     ≤ 40 km  → 25
 *     > 40 km  → 10
 *   If theatre coordinates are null → score = 50 (neutral fallback)
 *
 * WEATHER SCORE  (0–100)
 *   Base score depends on weather condition:
 *     Clear sky (code 0)           → 100  (perfect cinema weather)
 *     Partly cloudy (codes 1–3)    → 90   (good cinema weather)
 *     Foggy (codes 45, 48)         → 70
 *     Drizzle/Rain (codes 51–67)   → 85   (indoor cinema is ideal in rain!)
 *     Snow (codes 71–77)           → 80   (indoor activity is appealing)
 *     Showers/Storm (codes 80–99)  → 90   (strong incentive to go indoors)
 *     Unknown/Fallback             → 60   (conservative score)
 *   Temperature modifier (applied additively, capped at 100):
 *     < 5°C or > 38°C → -10  (uncomfortable extremes)
 *     5–15°C or 30–38°C → 0  (mild penalty/no modifier)
 *     15–30°C → +5  (comfortable temperature bonus)
 *
 * SHOWTIME SCORE  (0–100)
 *   Time ranges (local show time hour):
 *     MORNING:   05:00 – 11:59
 *     AFTERNOON: 12:00 – 16:59
 *     EVENING:   17:00 – 20:59
 *     NIGHT:     21:00 – 04:59 (wraps midnight)
 *   Scoring:
 *     Preference = ANY           → 75  (no preference expressed)
 *     Show matches preference    → 100
 *     Show does not match        → 30  (strong mismatch penalty)
 */
@Component
public class RecommendationEngine {

    // ──────────────────────────── PUBLIC API ────────────────────────────

    /**
     * Calculates a complete RecommendationResult for a single show/movie/theatre combination.
     *
     * @param show              the show to score
     * @param userLat           user's latitude
     * @param userLon           user's longitude
     * @param preferredGenres   user's preferred genre names (may be empty)
     * @param showtimePref      user's showtime preference
     * @param weather           current weather context (use WeatherContext.fallback() if unavailable)
     * @return fully scored RecommendationResult
     */
    public RecommendationResult score(
            ShowView show,
            double userLat, double userLon,
            Set<String> preferredGenres,
            ShowtimePreference showtimePref,
            WeatherContext weather) {

        MovieView movie = show.movie();
        TheatreView theatre = show.theatre();

        double ratingScore   = computeRatingScore(movie);
        double genreScore    = computeGenreScore(movie, preferredGenres);
        double distKm        = computeDistanceKm(theatre, userLat, userLon);
        double distanceScore = computeDistanceScore(distKm, theatre);
        double weatherScore  = computeWeatherScore(weather);
        double showtimeScore = computeShowtimeScore(show.showtime(), showtimePref);

        double experienceScore =
                ratingScore   * 0.25
              + genreScore    * 0.25
              + distanceScore * 0.20
              + weatherScore  * 0.15
              + showtimeScore * 0.15;

        // Round to 1 decimal place for display
        experienceScore = Math.round(experienceScore * 10.0) / 10.0;

        List<String> reasons = buildReasons(
                movie, theatre, ratingScore, genreScore,
                distanceScore, distKm, weatherScore, weather,
                showtimeScore, showtimePref, preferredGenres);

        return new RecommendationResult(
                movie, show, theatre,
                experienceScore,
                ratingScore, genreScore, distanceScore, weatherScore, showtimeScore,
                distKm, weather, reasons);
    }

    // ──────────────────────────── RATING ────────────────────────────

    /**
     * Normalises movie rating (0–10 scale) to 0–100.
     * Null rating → 50 (neutral).
     */
    public double computeRatingScore(MovieView movie) {
        if (movie.rating() == null) {
            return 50.0;
        }
        double score = (movie.rating() / 10.0) * 100.0;
        return clamp(score, 0.0, 100.0);
    }

    // ──────────────────────────── GENRE ────────────────────────────

    /**
     * Computes genre match score.
     *
     * If user has no preferred genres → 75 (broad interest).
     * If movie has no genres → 50 (neutral).
     * Otherwise: matchCount / max(preferredCount, movieGenreCount) * 100.
     */
    public double computeGenreScore(MovieView movie, Set<String> preferredGenres) {
        if (preferredGenres == null || preferredGenres.isEmpty()) {
            return 75.0; // User has no preference → moderate score
        }
        List<String> movieGenres = movie.genres();
        if (movieGenres == null || movieGenres.isEmpty()) {
            return 50.0; // Unknown genres → neutral
        }
        long matchCount = movieGenres.stream()
                .filter(preferredGenres::contains)
                .count();
        if (matchCount == 0) {
            return 20.0; // No genre match → low score
        }
        double denominator = Math.max(preferredGenres.size(), movieGenres.size());
        return clamp((matchCount / denominator) * 100.0, 0.0, 100.0);
    }

    // ──────────────────────────── DISTANCE ────────────────────────────

    /**
     * Computes Haversine distance from user to theatre.
     * Returns 0.0 if theatre coordinates are null.
     */
    public double computeDistanceKm(TheatreView theatre, double userLat, double userLon) {
        if (theatre.latitude() == null || theatre.longitude() == null) {
            return 0.0;
        }
        return HaversineCalculator.distanceKm(userLat, userLon,
                theatre.latitude(), theatre.longitude());
    }

    /**
     * Converts distance in km to a 0–100 score using threshold buckets.
     *
     * ≤ 2 km  → 100
     * ≤ 5 km  → 85
     * ≤ 10 km → 65
     * ≤ 20 km → 45
     * ≤ 40 km → 25
     * > 40 km → 10
     *
     * If theatre coordinates were null → 50 (neutral fallback).
     */
    public double computeDistanceScore(double distKm, TheatreView theatre) {
        if (theatre.latitude() == null || theatre.longitude() == null) {
            return 50.0; // No coordinates available
        }
        if (distKm <= 2.0)  return 100.0;
        if (distKm <= 5.0)  return 85.0;
        if (distKm <= 10.0) return 65.0;
        if (distKm <= 20.0) return 45.0;
        if (distKm <= 40.0) return 25.0;
        return 10.0;
    }

    // ──────────────────────────── WEATHER ────────────────────────────

    /**
     * Computes weather suitability score for indoor cinema attendance.
     *
     * Rain/snow/storms actually INCREASE the cinema score because
     * indoor entertainment becomes more attractive in bad weather.
     *
     * If weather is a fallback (API unavailable) → conservative score of 60.
     */
    public double computeWeatherScore(WeatherContext weather) {
        if (weather.isFallback()) {
            return 60.0; // Conservative neutral-ish score when API unavailable
        }
        double base = baseWeatherScore(weather.weatherCode());
        double tempMod = temperatureModifier(weather.temperatureCelsius());
        return clamp(base + tempMod, 0.0, 100.0);
    }

    private double baseWeatherScore(int code) {
        if (code == 0)                        return 100.0; // Clear sky
        if (code >= 1  && code <= 3)          return 90.0;  // Partly cloudy
        if (code == 45 || code == 48)         return 70.0;  // Fog
        if (code >= 51 && code <= 67)         return 85.0;  // Rain → indoor!
        if (code >= 71 && code <= 77)         return 80.0;  // Snow → indoor!
        if (code >= 80 && code <= 99)         return 90.0;  // Storms → indoor!
        return 60.0; // Unknown code
    }

    private double temperatureModifier(double tempC) {
        if (tempC < 5.0 || tempC > 38.0) return -10.0; // Extreme temperatures
        if (tempC >= 15.0 && tempC <= 30.0) return 5.0; // Comfortable range
        return 0.0; // Mild discomfort, no modifier
    }

    // ──────────────────────────── SHOWTIME ────────────────────────────

    /**
     * Computes showtime preference match score.
     *
     * Time ranges (show hour):
     *   MORNING:   05:00 – 11:59
     *   AFTERNOON: 12:00 – 16:59
     *   EVENING:   17:00 – 20:59
     *   NIGHT:     21:00+ or 00:00–04:59
     *
     * ANY preference → 75 (no preference expressed, moderate neutral score).
     * Match → 100.
     * No match → 30 (strong penalty for mismatched schedule).
     */
    public double computeShowtimeScore(LocalDateTime showtime, ShowtimePreference preference) {
        if (preference == null || preference == ShowtimePreference.ANY) {
            return 75.0;
        }
        if (showtime == null) {
            return 50.0;
        }
        ShowtimePreference actual = classifyShowtime(showtime);
        return (actual == preference) ? 100.0 : 30.0;
    }

    /**
     * Maps a show's hour to its ShowtimePreference category.
     */
    public ShowtimePreference classifyShowtime(LocalDateTime showtime) {
        int hour = showtime.getHour();
        if (hour >= 5  && hour < 12) return ShowtimePreference.MORNING;
        if (hour >= 12 && hour < 17) return ShowtimePreference.AFTERNOON;
        if (hour >= 17 && hour < 21) return ShowtimePreference.EVENING;
        return ShowtimePreference.NIGHT; // 21:00–04:59
    }

    // ──────────────────────────── REASONS ────────────────────────────

    private List<String> buildReasons(
            MovieView movie, TheatreView theatre,
            double ratingScore, double genreScore,
            double distanceScore, double distKm,
            double weatherScore, WeatherContext weather,
            double showtimeScore, ShowtimePreference pref,
            Set<String> preferredGenres) {

        List<String> reasons = new ArrayList<>();

        // Rating reason
        if (movie.rating() != null) {
            if (ratingScore >= 80) {
                reasons.add(String.format("Highly rated movie (%.1f/10)", movie.rating()));
            } else if (ratingScore >= 60) {
                reasons.add(String.format("Well-rated movie (%.1f/10)", movie.rating()));
            } else {
                reasons.add(String.format("Movie rating: %.1f/10", movie.rating()));
            }
        } else {
            reasons.add("Movie rating not available (neutral score applied)");
        }

        // Genre reason
        if (preferredGenres != null && !preferredGenres.isEmpty() && movie.genres() != null) {
            List<String> matched = movie.genres().stream()
                    .filter(preferredGenres::contains)
                    .toList();
            if (!matched.isEmpty()) {
                reasons.add("Matches preferred genre(s): " + String.join(", ", matched));
            } else {
                reasons.add("No matching preferred genres");
            }
        } else if (preferredGenres == null || preferredGenres.isEmpty()) {
            reasons.add("No genre preference set – all genres considered");
        }

        // Distance reason
        if (theatre.latitude() != null && theatre.longitude() != null) {
            if (distKm <= 2.0) {
                reasons.add(String.format("Theatre is very close (%.1f km)", distKm));
            } else if (distKm <= 10.0) {
                reasons.add(String.format("Theatre is nearby (%.1f km)", distKm));
            } else {
                reasons.add(String.format("Theatre is %.1f km away", distKm));
            }
        } else {
            reasons.add("Theatre location not available");
        }

        // Weather reason
        if (weather.isFallback()) {
            reasons.add("Weather data unavailable – conservative score applied");
        } else {
            String condition = weather.conditionDescription();
            double temp = weather.temperatureCelsius();
            if (weather.isRaining() || weather.isSnowing()) {
                reasons.add(String.format("%s (%.0f°C) – perfect day for an indoor cinema!", condition, temp));
            } else if (weatherScore >= 90) {
                reasons.add(String.format("%s (%.0f°C) – great cinema weather", condition, temp));
            } else {
                reasons.add(String.format("%s (%.0f°C)", condition, temp));
            }
        }

        // Showtime reason
        if (pref == null || pref == ShowtimePreference.ANY) {
            reasons.add("No showtime preference – any time works");
        } else if (showtimeScore >= 100) {
            reasons.add("Showtime matches your " + pref.name().toLowerCase() + " preference");
        } else {
            reasons.add("Showtime does not match your " + pref.name().toLowerCase() + " preference");
        }

        return List.copyOf(reasons);
    }

    // ──────────────────────────── UTILITY ────────────────────────────

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
