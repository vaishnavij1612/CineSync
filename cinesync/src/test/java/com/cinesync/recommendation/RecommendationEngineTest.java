package com.cinesync.recommendation;

import com.cinesync.dto.MovieView;
import com.cinesync.dto.ShowView;
import com.cinesync.dto.TheatreView;
import com.cinesync.model.ShowtimePreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RecommendationEngine}.
 *
 * Tests are deterministic – no mocking, no randomness.
 * Each scoring component is tested individually, and the
 * final weighted score formula is validated.
 */
class RecommendationEngineTest {

    private RecommendationEngine engine;

    // Helper views
    private static MovieView movieWithRating(double rating, List<String> genres) {
        return new MovieView(1L, "Test Movie", "Overview", rating,
                null, null, genres);
    }

    private static MovieView movieNullRating() {
        return new MovieView(1L, "Unknown Rating", "Overview", null,
                null, null, List.of("Drama"));
    }

    private static TheatreView theatreAt(double lat, double lon) {
        return new TheatreView(1L, "Test Theatre", "Addr", lat, lon, "ACTIVE");
    }

    private static TheatreView theatreNoCoords() {
        return new TheatreView(1L, "Unknown Theatre", "Addr", null, null, "ACTIVE");
    }

    private static ShowView showAt(LocalDateTime time, TheatreView theatre, MovieView movie) {
        return new ShowView(1L, movie, theatre, time, "SCHEDULED", 20, 0);
    }

    @BeforeEach
    void setUp() {
        engine = new RecommendationEngine();
    }

    // ──────────────────────────── RATING ────────────────────────────

    @Test
    void ratingScore_perfectRatingGives100() {
        double score = engine.computeRatingScore(movieWithRating(10.0, List.of()));
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void ratingScore_zeroRatingGivesZero() {
        double score = engine.computeRatingScore(movieWithRating(0.0, List.of()));
        assertEquals(0.0, score, 0.001);
    }

    @Test
    void ratingScore_midRatingGives50() {
        double score = engine.computeRatingScore(movieWithRating(5.0, List.of()));
        assertEquals(50.0, score, 0.001);
    }

    @Test
    void ratingScore_nullRatingGivesNeutral50() {
        double score = engine.computeRatingScore(movieNullRating());
        assertEquals(50.0, score, 0.001);
    }

    @Test
    void ratingScore_8point7Gives87() {
        double score = engine.computeRatingScore(movieWithRating(8.7, List.of()));
        assertEquals(87.0, score, 0.001);
    }

    // ──────────────────────────── GENRE ────────────────────────────

    @Test
    void genreScore_noPreferencesGives75() {
        double score = engine.computeGenreScore(movieWithRating(7.0, List.of("Action", "Drama")),
                Set.of());
        assertEquals(75.0, score, 0.001);
    }

    @Test
    void genreScore_perfectMatchGives100() {
        // 2/2 preferred genres match (max of 2 preferred, 2 movie)
        double score = engine.computeGenreScore(
                movieWithRating(7.0, List.of("Action", "Drama")),
                Set.of("Action", "Drama"));
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void genreScore_noMatchGives20() {
        double score = engine.computeGenreScore(
                movieWithRating(7.0, List.of("Sci-Fi")),
                Set.of("Comedy", "Thriller"));
        assertEquals(20.0, score, 0.001);
    }

    @Test
    void genreScore_partialMatch() {
        // 1 of 2 preferred matches, movie has 1 genre
        // denominator = max(2, 1) = 2 → 1/2 * 100 = 50
        double score = engine.computeGenreScore(
                movieWithRating(7.0, List.of("Action")),
                Set.of("Action", "Drama"));
        assertEquals(50.0, score, 0.001);
    }

    @Test
    void genreScore_movieNoGenresGives50() {
        double score = engine.computeGenreScore(
                movieWithRating(7.0, List.of()),
                Set.of("Action"));
        assertEquals(50.0, score, 0.001);
    }

    // ──────────────────────────── DISTANCE ────────────────────────────

    @Test
    void distanceScore_veryClose2kmGives100() {
        // Same coordinates → 0 km → 100
        double score = engine.computeDistanceScore(1.5, theatreAt(12.97, 77.59));
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void distanceScore_within5kmGives85() {
        double score = engine.computeDistanceScore(3.0, theatreAt(12.97, 77.59));
        assertEquals(85.0, score, 0.001);
    }

    @Test
    void distanceScore_within10kmGives65() {
        double score = engine.computeDistanceScore(8.0, theatreAt(12.97, 77.59));
        assertEquals(65.0, score, 0.001);
    }

    @Test
    void distanceScore_within20kmGives45() {
        double score = engine.computeDistanceScore(15.0, theatreAt(12.97, 77.59));
        assertEquals(45.0, score, 0.001);
    }

    @Test
    void distanceScore_within40kmGives25() {
        double score = engine.computeDistanceScore(30.0, theatreAt(12.97, 77.59));
        assertEquals(25.0, score, 0.001);
    }

    @Test
    void distanceScore_over40kmGives10() {
        double score = engine.computeDistanceScore(50.0, theatreAt(12.97, 77.59));
        assertEquals(10.0, score, 0.001);
    }

    @Test
    void distanceScore_nullCoordsGivesNeutral50() {
        double score = engine.computeDistanceScore(0.0, theatreNoCoords());
        assertEquals(50.0, score, 0.001);
    }

    // ──────────────────────────── WEATHER ────────────────────────────

    @Test
    void weatherScore_clearSkyCodes100() {
        WeatherContext clear = new WeatherContext(22.0, 0.0, 0, 5.0, false);
        double score = engine.computeWeatherScore(clear);
        // base=100 + tempMod=+5 = 105 → clamped to 100
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void weatherScore_rainGivesHighScore() {
        // Rain → indoor cinema incentive
        WeatherContext rain = new WeatherContext(18.0, 5.0, 61, 10.0, false);
        double score = engine.computeWeatherScore(rain);
        // base=85 + tempMod=+5 (18°C is in 15-30) = 90
        assertEquals(90.0, score, 0.001);
    }

    @Test
    void weatherScore_stormGivesHighScore() {
        WeatherContext storm = new WeatherContext(22.0, 15.0, 95, 30.0, false);
        double score = engine.computeWeatherScore(storm);
        // base=90 + tempMod=+5 = 95
        assertEquals(95.0, score, 0.001);
    }

    @Test
    void weatherScore_extremeHeatPenalty() {
        WeatherContext hot = new WeatherContext(40.0, 0.0, 0, 0.0, false);
        double score = engine.computeWeatherScore(hot);
        // base=100 + tempMod=-10 = 90
        assertEquals(90.0, score, 0.001);
    }

    @Test
    void weatherScore_fallbackGivesConservative60() {
        WeatherContext fallback = WeatherContext.fallback();
        double score = engine.computeWeatherScore(fallback);
        assertEquals(60.0, score, 0.001);
    }

    // ──────────────────────────── SHOWTIME ────────────────────────────

    @Test
    void showtimeScore_anyPreferenceGives75() {
        LocalDateTime evening = LocalDateTime.of(2025, 1, 1, 19, 0);
        double score = engine.computeShowtimeScore(evening, ShowtimePreference.ANY);
        assertEquals(75.0, score, 0.001);
    }

    @Test
    void showtimeScore_matchingEveningGives100() {
        LocalDateTime evening = LocalDateTime.of(2025, 1, 1, 19, 0);
        double score = engine.computeShowtimeScore(evening, ShowtimePreference.EVENING);
        assertEquals(100.0, score, 0.001);
    }

    @Test
    void showtimeScore_mismatchGives30() {
        LocalDateTime evening = LocalDateTime.of(2025, 1, 1, 19, 0);
        double score = engine.computeShowtimeScore(evening, ShowtimePreference.MORNING);
        assertEquals(30.0, score, 0.001);
    }

    @Test
    void showtimeClassification_morningHour() {
        assertEquals(ShowtimePreference.MORNING,
                engine.classifyShowtime(LocalDateTime.of(2025, 1, 1, 9, 0)));
    }

    @Test
    void showtimeClassification_afternoonHour() {
        assertEquals(ShowtimePreference.AFTERNOON,
                engine.classifyShowtime(LocalDateTime.of(2025, 1, 1, 14, 0)));
    }

    @Test
    void showtimeClassification_eveningHour() {
        assertEquals(ShowtimePreference.EVENING,
                engine.classifyShowtime(LocalDateTime.of(2025, 1, 1, 18, 30)));
    }

    @Test
    void showtimeClassification_nightHour() {
        assertEquals(ShowtimePreference.NIGHT,
                engine.classifyShowtime(LocalDateTime.of(2025, 1, 1, 22, 0)));
    }

    @Test
    void showtimeClassification_midnightIsNight() {
        assertEquals(ShowtimePreference.NIGHT,
                engine.classifyShowtime(LocalDateTime.of(2025, 1, 1, 0, 30)));
    }

    // ──────────────────────────── FINAL SCORE ────────────────────────────

    @Test
    void finalScore_weightedFormulaIsCorrect() {
        // Construct a scenario where all scores are known:
        // movie rating = 8.0 → ratingScore = 80
        // genres: [Action] preferred: [Action] → 1/max(1,1)*100 = 100
        // distance = 1.0 km → 100
        // weather clear 22°C → 100 (capped)
        // showtime EVENING preference + evening show → 100
        // experienceScore = 80*0.25 + 100*0.25 + 100*0.20 + 100*0.15 + 100*0.15
        //                 = 20 + 25 + 20 + 15 + 15 = 95.0

        MovieView movie = new MovieView(1L, "Dune", "Epic", 8.0, null, null, List.of("Action"));
        TheatreView theatre = new TheatreView(1L, "Cinema", "Addr", 12.9716, 77.5946, "ACTIVE");
        LocalDateTime eveningTime = LocalDateTime.of(2025, 6, 1, 19, 0);
        ShowView show = new ShowView(1L, movie, theatre, eveningTime, "SCHEDULED", 20, 0);
        WeatherContext weather = new WeatherContext(22.0, 0.0, 0, 0.0, false);

        RecommendationResult result = engine.score(
                show, 12.9716, 77.5946,  // same coords → dist 0 km → distanceScore = 100
                Set.of("Action"),
                ShowtimePreference.EVENING,
                weather);

        assertEquals(95.0, result.experienceScore(), 0.5,
                "Expected experienceScore ≈ 95 for perfect-match scenario");
        assertEquals(80.0, result.ratingScore(), 0.001);
        assertEquals(100.0, result.genreScore(), 0.001);
        assertEquals(100.0, result.distanceScore(), 0.001); // ≤ 2 km
        assertEquals(100.0, result.showtimeScore(), 0.001);
    }

    @Test
    void finalScore_hasNonEmptyReasons() {
        MovieView movie = new MovieView(1L, "Test", "Desc", 7.5, null, null, List.of("Drama"));
        TheatreView theatre = new TheatreView(1L, "Cinema", "Addr", 12.9716, 77.5946, "ACTIVE");
        LocalDateTime time = LocalDateTime.of(2025, 6, 1, 20, 0);
        ShowView show = new ShowView(1L, movie, theatre, time, "SCHEDULED", 20, 0);

        RecommendationResult result = engine.score(
                show, 12.9716, 77.5946,
                Set.of("Drama"),
                ShowtimePreference.EVENING,
                WeatherContext.fallback());

        assertFalse(result.reasons().isEmpty(), "Reasons list should not be empty");
        assertEquals(5, result.reasons().size(), "Expected exactly 5 reason lines (one per component)");
    }

    @Test
    void explanationMentionsWeatherFallback() {
        MovieView movie = new MovieView(1L, "Test", "Desc", 7.0, null, null, List.of("Comedy"));
        TheatreView theatre = new TheatreView(1L, "Cinema", "Addr", 12.9716, 77.5946, "ACTIVE");
        ShowView show = new ShowView(1L, movie, theatre,
                LocalDateTime.of(2025, 6, 1, 19, 0), "SCHEDULED", 20, 0);

        RecommendationResult result = engine.score(
                show, 12.9716, 77.5946,
                Set.of("Comedy"),
                ShowtimePreference.ANY,
                WeatherContext.fallback());

        boolean hasFallbackReason = result.reasons().stream()
                .anyMatch(r -> r.toLowerCase().contains("unavailable") || r.toLowerCase().contains("fallback"));
        assertTrue(hasFallbackReason, "Reasons should mention weather fallback: " + result.reasons());
    }
}
