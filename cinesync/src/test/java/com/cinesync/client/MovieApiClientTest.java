package com.cinesync.client;

import com.cinesync.client.dto.TmdbMovieDto;
import com.cinesync.client.dto.TmdbSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link MovieApiClient}.
 * External HTTP calls are fully mocked – no live internet required.
 */
class MovieApiClientTest {

    private RestTemplate mockRestTemplate;
    private MovieApiClient client;

    @BeforeEach
    void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
    }

    private MovieApiClient clientWithKey(String key) {
        return new MovieApiClient(mockRestTemplate,
                "https://api.themoviedb.org/3",
                "https://image.tmdb.org/t/p/w500",
                key);
    }

    @Test
    void searchMovies_noApiKeyReturnsEmptyList() {
        client = clientWithKey(""); // blank key

        List<TmdbMovieDto> results = client.searchMovies("Dune");

        assertTrue(results.isEmpty());
        verifyNoInteractions(mockRestTemplate);
    }

    @Test
    void searchMovies_successReturnsResults() {
        client = clientWithKey("test-api-key");

        TmdbMovieDto movie = new TmdbMovieDto();
        TmdbSearchResponse response = new TmdbSearchResponse();
        // Use reflection or direct field access – we need to set the results
        // We'll verify the client parses a non-null response correctly.
        when(mockRestTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(buildResponse("Dune: Part Two", 8.7));

        List<TmdbMovieDto> results = client.searchMovies("Dune");

        assertEquals(1, results.size());
        assertEquals("Dune: Part Two", results.get(0).getTitle());
    }

    @Test
    void searchMovies_nullResponseReturnsEmptyList() {
        client = clientWithKey("test-api-key");
        when(mockRestTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(null);

        List<TmdbMovieDto> results = client.searchMovies("anything");

        assertTrue(results.isEmpty());
    }

    @Test
    void searchMovies_connectionFailureReturnsEmptyList() {
        client = clientWithKey("test-api-key");
        when(mockRestTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Should NOT throw – graceful failure
        List<TmdbMovieDto> results = client.searchMovies("Dune");

        assertTrue(results.isEmpty());
    }

    @Test
    void discoverPopular_noApiKeyReturnsEmptyList() {
        client = clientWithKey("");
        assertTrue(client.discoverPopular().isEmpty());
        verifyNoInteractions(mockRestTemplate);
    }

    @Test
    void discoverPopular_successReturnsResults() {
        client = clientWithKey("test-api-key");
        when(mockRestTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(buildResponse("Popular Movie", 7.5));

        List<TmdbMovieDto> results = client.discoverPopular();

        assertEquals(1, results.size());
    }

    @Test
    void isConfigured_withApiKeyReturnsTrue() {
        client = clientWithKey("some-key");
        assertTrue(client.isConfigured());
    }

    @Test
    void isConfigured_withoutApiKeyReturnsFalse() {
        client = clientWithKey("");
        assertFalse(client.isConfigured());
    }

    @Test
    void posterUrlIsBuiltFromPosterPath() {
        client = clientWithKey("test-api-key");
        TmdbSearchResponse response = buildResponse("Test Movie", 7.0);
        response.getResults().get(0).setPosterUrl(null); // reset
        // Manually set a poster path by building a custom response
        TmdbSearchResponse withPoster = buildResponseWithPoster("Test", 7.0, "/abc.jpg");
        when(mockRestTemplate.getForObject(anyString(), eq(TmdbSearchResponse.class)))
                .thenReturn(withPoster);

        List<TmdbMovieDto> results = client.searchMovies("Test");

        assertNotNull(results.get(0).getPosterUrl());
        assertTrue(results.get(0).getPosterUrl().startsWith("https://image.tmdb.org/t/p/w500"));
    }

    // ──────────── helpers ────────────

    private static TmdbSearchResponse buildResponse(String title, double rating) {
        return buildResponseWithPoster(title, rating, null);
    }

    private static TmdbSearchResponse buildResponseWithPoster(String title, double rating, String posterPath) {
        // Build via inner classes using a test-only constructor approach
        TmdbMovieDto dto = createDto(title, rating, posterPath);
        TmdbSearchResponse response = new TmdbSearchResponse();
        // Use reflection to set results field (since TmdbSearchResponse uses Jackson binding)
        try {
            var field = TmdbSearchResponse.class.getDeclaredField("results");
            field.setAccessible(true);
            field.set(response, List.of(dto));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private static TmdbMovieDto createDto(String title, double rating, String posterPath) {
        TmdbMovieDto dto = new TmdbMovieDto();
        try {
            setField(dto, "title", title);
            setField(dto, "voteAverage", rating);
            setField(dto, "posterPath", posterPath);
            setField(dto, "externalId", 12345L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dto;
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
