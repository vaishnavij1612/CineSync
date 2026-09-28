package com.cinesync.service;

import com.cinesync.client.WeatherApiClient;
import com.cinesync.dto.MovieView;
import com.cinesync.dto.PreferenceView;
import com.cinesync.dto.ShowView;
import com.cinesync.dto.TheatreView;
import com.cinesync.model.ShowtimePreference;
import com.cinesync.recommendation.RecommendationEngine;
import com.cinesync.recommendation.RecommendationResult;
import com.cinesync.recommendation.WeatherContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RecommendationService}.
 *
 * All dependencies are mocked. No database, no external APIs.
 * Tests cover: successful recommendations, weather fallback,
 * empty show list, and correct use of authenticated username.
 */
class RecommendationServiceTest {

    private PreferenceService mockPreferenceService;
    private ShowService mockShowService;
    private WeatherApiClient mockWeatherApiClient;
    private RecommendationEngine realEngine;  // Use real engine for integration-style scoring
    private RecommendationService service;

    @BeforeEach
    void setUp() {
        mockPreferenceService = mock(PreferenceService.class);
        mockShowService = mock(ShowService.class);
        mockWeatherApiClient = mock(WeatherApiClient.class);
        realEngine = new RecommendationEngine();
        service = new RecommendationService(
                mockPreferenceService, mockShowService,
                mockWeatherApiClient, realEngine);
    }

    @Test
    void recommend_successfulPipelineReturnsSortedResults() {
        // Arrange
        PreferenceView prefs = new PreferenceView(
                List.of(1L), List.of("Action"), ShowtimePreference.EVENING);
        when(mockPreferenceService.getCurrentPreferences("alice")).thenReturn(prefs);
        when(mockWeatherApiClient.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(new WeatherContext(22.0, 0.0, 0, 5.0, false)));

        ShowView show1 = buildShow(1L, "High Rated Action", 9.0, List.of("Action"),
                LocalDateTime.of(2025, 6, 1, 19, 0), 12.9716, 77.5946);
        ShowView show2 = buildShow(2L, "Low Rated Comedy", 4.0, List.of("Comedy"),
                LocalDateTime.of(2025, 6, 1, 9, 0), 12.9352, 77.6245);
        when(mockShowService.findUpcoming()).thenReturn(List.of(show1, show2));

        // Act
        List<RecommendationResult> results = service.recommend("alice", 12.9716, 77.5946);

        // Assert
        assertEquals(2, results.size());
        // High-rated Action evening show should rank first
        assertTrue(results.get(0).experienceScore() > results.get(1).experienceScore(),
                "Higher-scoring show should be ranked first");
        assertEquals("High Rated Action", results.get(0).movie().title());
    }

    @Test
    void recommend_weatherApiFallsBackGracefully() {
        // Arrange
        PreferenceView prefs = new PreferenceView(
                List.of(), List.of(), ShowtimePreference.ANY);
        when(mockPreferenceService.getCurrentPreferences("bob")).thenReturn(prefs);
        // Weather API fails
        when(mockWeatherApiClient.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());

        ShowView show = buildShow(1L, "Some Movie", 7.0, List.of("Drama"),
                LocalDateTime.of(2025, 6, 1, 15, 0), 12.97, 77.59);
        when(mockShowService.findUpcoming()).thenReturn(List.of(show));

        // Act – should NOT throw
        List<RecommendationResult> results = service.recommend("bob", 12.97, 77.59);

        // Assert
        assertEquals(1, results.size());
        // Weather context should be the fallback
        assertTrue(results.get(0).weatherContext().isFallback(),
                "Should use fallback weather when API is unavailable");
    }

    @Test
    void recommend_noShowsReturnsEmptyList() {
        PreferenceView prefs = new PreferenceView(
                List.of(), List.of(), ShowtimePreference.ANY);
        when(mockPreferenceService.getCurrentPreferences("carol")).thenReturn(prefs);
        when(mockWeatherApiClient.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(WeatherContext.fallback()));
        when(mockShowService.findUpcoming()).thenReturn(List.of());

        List<RecommendationResult> results = service.recommend("carol", 0.0, 0.0);

        assertTrue(results.isEmpty());
    }

    @Test
    void recommend_usesAuthenticatedUsernameForPreferences() {
        PreferenceView prefs = new PreferenceView(
                List.of(), List.of(), ShowtimePreference.ANY);
        when(mockPreferenceService.getCurrentPreferences("dave")).thenReturn(prefs);
        when(mockWeatherApiClient.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(Optional.empty());
        when(mockShowService.findUpcoming()).thenReturn(List.of());

        service.recommend("dave", 0.0, 0.0);

        // Verify preferences were loaded for the correct username
        verify(mockPreferenceService).getCurrentPreferences("dave");
        verify(mockPreferenceService, never()).getCurrentPreferences("alice");
    }

    @Test
    void recommend_genrePreferenceInfluencesScore() {
        // User prefers Sci-Fi
        PreferenceView prefs = new PreferenceView(
                List.of(4L), List.of("Sci-Fi"), ShowtimePreference.ANY);
        when(mockPreferenceService.getCurrentPreferences("eve")).thenReturn(prefs);
        when(mockWeatherApiClient.getCurrentWeather(anyDouble(), anyDouble()))
                .thenReturn(Optional.of(new WeatherContext(20.0, 0.0, 0, 0.0, false)));

        ShowView scifiShow = buildShow(1L, "Dune", 8.5, List.of("Sci-Fi"),
                LocalDateTime.of(2025, 6, 1, 19, 0), 12.97, 77.59);
        ShowView dramaShow = buildShow(2L, "Drama Film", 8.5, List.of("Drama"),
                LocalDateTime.of(2025, 6, 1, 19, 0), 12.97, 77.59);
        when(mockShowService.findUpcoming()).thenReturn(List.of(scifiShow, dramaShow));

        List<RecommendationResult> results = service.recommend("eve", 12.97, 77.59);

        assertEquals(2, results.size());
        // Sci-Fi show (genre match) should rank higher
        assertEquals("Dune", results.get(0).movie().title(),
                "Sci-Fi movie should rank first for user who prefers Sci-Fi");
    }

    // ──────────── helpers ────────────

    private static ShowView buildShow(Long id, String title, double rating, List<String> genres,
                                      LocalDateTime showtime, double theatreLat, double theatreLon) {
        MovieView movie = new MovieView(id, title, "Overview", rating, null, null, genres);
        TheatreView theatre = new TheatreView(id, "Cinema " + id, "Addr",
                theatreLat, theatreLon, "ACTIVE");
        return new ShowView(id, movie, theatre, showtime, "SCHEDULED", 20, 0);
    }
}
