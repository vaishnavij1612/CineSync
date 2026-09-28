package com.cinesync.controller;

import com.cinesync.client.MovieApiClient;
import com.cinesync.config.SecurityConfig;
import com.cinesync.dto.MovieView;
import com.cinesync.dto.ShowView;
import com.cinesync.dto.TheatreView;
import com.cinesync.recommendation.RecommendationEngine;
import com.cinesync.recommendation.RecommendationResult;
import com.cinesync.recommendation.WeatherContext;
import com.cinesync.service.DatabaseUserDetailsService;
import com.cinesync.service.RecommendationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link RecommendationApiController}.
 *
 * Uses @WebMvcTest – no database required.
 * All service layer dependencies are mocked.
 */
@WebMvcTest(RecommendationApiController.class)
@Import(SecurityConfig.class)
class RecommendationApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecommendationService recommendationService;

    @MockitoBean
    private MovieApiClient movieApiClient;

    @MockitoBean
    private DatabaseUserDetailsService databaseUserDetailsService;

    @Test
    @WithMockUser(username = "alice")
    void getRecommendations_authenticatedUserReceivesResults() throws Exception {
        RecommendationResult result = buildResult("Dune", 8.7);
        when(recommendationService.recommend(eq("alice"), anyDouble(), anyDouble()))
                .thenReturn(List.of(result));

        mockMvc.perform(get("/api/recommendations")
                        .param("lat", "12.9716")
                        .param("lon", "77.5946"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieTitle").value("Dune"))
                .andExpect(jsonPath("$[0].experienceScore").isNumber());
    }

    @Test
    void getRecommendations_unauthenticatedReturns401or3xx() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                        .param("lat", "12.9716")
                        .param("lon", "77.5946"))
                .andExpect(result ->
                        assertTrue(result.getResponse().getStatus() == 401
                                || result.getResponse().getStatus() == 302,
                                "Expected 401 or redirect for unauthenticated request"));
    }

    @Test
    @WithMockUser(username = "alice")
    void getRecommendations_emptyResultsReturnsEmptyArray() throws Exception {
        when(recommendationService.recommend(anyString(), anyDouble(), anyDouble()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/recommendations")
                        .param("lat", "12.9716")
                        .param("lon", "77.5946"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(username = "alice")
    void discoverMovies_notConfiguredReturnsEmptyWithMessage() throws Exception {
        when(movieApiClient.isConfigured()).thenReturn(false);
        when(movieApiClient.searchMovies(anyString())).thenReturn(List.of());
        when(movieApiClient.discoverPopular()).thenReturn(List.of());

        mockMvc.perform(get("/api/discover/movies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.configured").value(false));
    }

    @Test
    @WithMockUser(username = "alice")
    void discoverMovies_withQueryCallsSearchMovies() throws Exception {
        when(movieApiClient.isConfigured()).thenReturn(true);
        when(movieApiClient.searchMovies("Dune")).thenReturn(List.of());

        mockMvc.perform(get("/api/discover/movies").param("query", "Dune"))
                .andExpect(status().isOk());

        verify(movieApiClient).searchMovies("Dune");
        verify(movieApiClient, never()).discoverPopular();
    }

    @Test
    @WithMockUser(username = "alice")
    void discoverMovies_withoutQueryCallsDiscoverPopular() throws Exception {
        when(movieApiClient.isConfigured()).thenReturn(true);
        when(movieApiClient.discoverPopular()).thenReturn(List.of());

        mockMvc.perform(get("/api/discover/movies"))
                .andExpect(status().isOk());

        verify(movieApiClient).discoverPopular();
        verify(movieApiClient, never()).searchMovies(anyString());
    }

    // ──────────── helpers ────────────

    private RecommendationResult buildResult(String movieTitle, double movieRating) {
        MovieView movie = new MovieView(1L, movieTitle, "Overview", movieRating,
                null, null, List.of("Action"));
        TheatreView theatre = new TheatreView(1L, "Cinema", "Addr", 12.9716, 77.5946, "ACTIVE");
        ShowView show = new ShowView(1L, movie, theatre,
                LocalDateTime.of(2025, 6, 1, 19, 0), "SCHEDULED", 20, 0);
        WeatherContext weather = new WeatherContext(22.0, 0.0, 0, 0.0, false);

        RecommendationEngine engine = new RecommendationEngine();
        return engine.score(show, 12.9716, 77.5946, Set.of("Action"),
                com.cinesync.model.ShowtimePreference.EVENING, weather);
    }
}
