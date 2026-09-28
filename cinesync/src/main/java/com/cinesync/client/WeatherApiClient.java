package com.cinesync.client;

import com.cinesync.client.dto.OpenMeteoResponse;
import com.cinesync.recommendation.WeatherContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * HTTP client for the Open-Meteo weather API.
 *
 * Open-Meteo is a free, no-API-key-required weather service.
 * Documentation: https://open-meteo.com/en/docs
 *
 * Configuration (application.properties):
 *   weather.base-url – defaults to https://api.open-meteo.com/v1
 *
 * Returns Optional.empty() on any failure so the recommendation engine
 * can continue with an explicit fallback weather state.
 */
@Component
public class WeatherApiClient {

    private static final Logger log = LoggerFactory.getLogger(WeatherApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public WeatherApiClient(
            RestTemplate externalApiRestTemplate,
            @Value("${weather.base-url:https://api.open-meteo.com/v1}") String baseUrl) {
        this.restTemplate = externalApiRestTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Fetches current weather conditions for the given coordinates.
     *
     * @param latitude  user/theatre latitude
     * @param longitude user/theatre longitude
     * @return Optional containing WeatherContext, or empty on failure
     */
    public Optional<WeatherContext> getCurrentWeather(double latitude, double longitude) {
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/forecast")
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("current", "temperature_2m,precipitation,weather_code,wind_speed_10m")
                    .queryParam("forecast_days", 1)
                    .build()
                    .toUriString();

            OpenMeteoResponse response = restTemplate.getForObject(url, OpenMeteoResponse.class);
            if (response == null || response.getCurrent() == null) {
                log.warn("Open-Meteo returned null/empty response for lat={} lon={}", latitude, longitude);
                return Optional.empty();
            }

            OpenMeteoResponse.CurrentWeather current = response.getCurrent();
            WeatherContext ctx = new WeatherContext(
                    current.getTemperature() != null ? current.getTemperature() : 20.0,
                    current.getPrecipitation() != null ? current.getPrecipitation() : 0.0,
                    current.getWeatherCode() != null ? current.getWeatherCode() : 0,
                    current.getWindSpeed() != null ? current.getWindSpeed() : 0.0,
                    false  // not a fallback
            );
            log.debug("Weather at ({},{}) – temp={}°C precip={}mm code={}",
                    latitude, longitude,
                    ctx.temperatureCelsius(), ctx.precipitationMm(), ctx.weatherCode());
            return Optional.of(ctx);

        } catch (ResourceAccessException e) {
            log.warn("Open-Meteo is unreachable (timeout/connection refused): {}", e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Open-Meteo returned an error response: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
