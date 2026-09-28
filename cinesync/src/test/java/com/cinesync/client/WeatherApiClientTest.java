package com.cinesync.client;

import com.cinesync.client.dto.OpenMeteoResponse;
import com.cinesync.recommendation.WeatherContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WeatherApiClient}.
 * All HTTP calls are mocked – no live internet required.
 */
class WeatherApiClientTest {

    private RestTemplate mockRestTemplate;
    private WeatherApiClient client;

    @BeforeEach
    void setUp() {
        mockRestTemplate = mock(RestTemplate.class);
        client = new WeatherApiClient(mockRestTemplate, "https://api.open-meteo.com/v1");
    }

    @Test
    void getCurrentWeather_successReturnsContext() {
        when(mockRestTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(buildResponse(22.5, 0.0, 0, 10.0));

        Optional<WeatherContext> result = client.getCurrentWeather(12.97, 77.59);

        assertTrue(result.isPresent());
        WeatherContext ctx = result.get();
        assertEquals(22.5, ctx.temperatureCelsius(), 0.001);
        assertEquals(0.0, ctx.precipitationMm(), 0.001);
        assertEquals(0, ctx.weatherCode());
        assertFalse(ctx.isFallback());
    }

    @Test
    void getCurrentWeather_nullResponseReturnsEmpty() {
        when(mockRestTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(null);

        Optional<WeatherContext> result = client.getCurrentWeather(12.97, 77.59);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentWeather_nullCurrentReturnsEmpty() {
        OpenMeteoResponse response = new OpenMeteoResponse();
        // current is null by default
        when(mockRestTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(response);

        Optional<WeatherContext> result = client.getCurrentWeather(12.97, 77.59);

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrentWeather_connectionFailureReturnsEmpty() {
        when(mockRestTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        // Must NOT throw – graceful failure
        Optional<WeatherContext> result = client.getCurrentWeather(12.97, 77.59);

        assertTrue(result.isEmpty());
    }

    @Test
    void weatherFallback_isNotFallbackWhenApiSucceeds() {
        when(mockRestTemplate.getForObject(anyString(), eq(OpenMeteoResponse.class)))
                .thenReturn(buildResponse(18.0, 5.0, 61, 12.0));

        Optional<WeatherContext> result = client.getCurrentWeather(12.97, 77.59);

        assertTrue(result.isPresent());
        assertFalse(result.get().isFallback(), "Real API response should not be marked as fallback");
    }

    @Test
    void weatherContext_rainyCodeIsIdentifiedAsRaining() {
        WeatherContext rain = new WeatherContext(18.0, 5.0, 61, 10.0, false);
        assertTrue(rain.isRaining());
        assertFalse(rain.isSnowing());
    }

    @Test
    void weatherContext_stormCodeIsIdentifiedAsRaining() {
        WeatherContext storm = new WeatherContext(20.0, 20.0, 95, 30.0, false);
        assertTrue(storm.isRaining());
    }

    @Test
    void weatherContext_snowCodeIsIdentifiedAsSnowing() {
        WeatherContext snow = new WeatherContext(-2.0, 0.0, 73, 5.0, false);
        assertTrue(snow.isSnowing());
        assertFalse(snow.isRaining());
    }

    @Test
    void weatherFallbackFactory_isFallbackFlagSet() {
        WeatherContext fallback = WeatherContext.fallback();
        assertTrue(fallback.isFallback());
        assertEquals(20.0, fallback.temperatureCelsius(), 0.001);
    }

    // ──────────── helpers ────────────

    private static OpenMeteoResponse buildResponse(double temp, double precip, int code, double wind) {
        OpenMeteoResponse response = new OpenMeteoResponse();
        try {
            OpenMeteoResponse.CurrentWeather current = createCurrent(temp, precip, code, wind);
            var field = OpenMeteoResponse.class.getDeclaredField("current");
            field.setAccessible(true);
            field.set(response, current);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private static OpenMeteoResponse.CurrentWeather createCurrent(
            double temp, double precip, int code, double wind) {
        OpenMeteoResponse.CurrentWeather current = new OpenMeteoResponse.CurrentWeather();
        try {
            setField(current, "temperature", temp);
            setField(current, "precipitation", precip);
            setField(current, "weatherCode", code);
            setField(current, "windSpeed", wind);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return current;
    }

    private static void setField(Object obj, String fieldName, Object value) throws Exception {
        var field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }
}
