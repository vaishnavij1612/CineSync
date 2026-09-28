package com.cinesync.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * External DTO representing the Open-Meteo weather API response.
 * Open-Meteo is free and does not require an API key.
 * This is NOT a JPA entity.
 *
 * Example Open-Meteo current weather endpoint:
 *   GET https://api.open-meteo.com/v1/forecast
 *       ?latitude={lat}&longitude={lon}
 *       &current=temperature_2m,precipitation,weather_code,wind_speed_10m
 *       &forecast_days=1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse {

    @JsonProperty("current")
    private CurrentWeather current;

    public CurrentWeather getCurrent() { return current; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {

        /** Temperature in degrees Celsius */
        @JsonProperty("temperature_2m")
        private Double temperature;

        /** Precipitation in mm */
        @JsonProperty("precipitation")
        private Double precipitation;

        /**
         * WMO Weather interpretation code.
         * 0 = Clear, 1-3 = Partly cloudy, 45/48 = Fog,
         * 51-67 = Drizzle/Rain, 71-77 = Snow, 80-99 = Showers/Thunderstorm
         */
        @JsonProperty("weather_code")
        private Integer weatherCode;

        /** Wind speed in km/h */
        @JsonProperty("wind_speed_10m")
        private Double windSpeed;

        public Double getTemperature() { return temperature; }
        public Double getPrecipitation() { return precipitation; }
        public Integer getWeatherCode() { return weatherCode; }
        public Double getWindSpeed() { return windSpeed; }
    }
}
