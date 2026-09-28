package com.cinesync.recommendation;

/**
 * Immutable value object representing the current weather conditions
 * used by the recommendation engine.
 *
 * @param temperatureCelsius  air temperature in °C
 * @param precipitationMm     precipitation in mm (last hour or current period)
 * @param weatherCode         WMO weather interpretation code
 *                            (0=clear, 1-3=cloudy, 45/48=fog,
 *                             51-67=drizzle/rain, 71-77=snow, 80-99=showers/storm)
 * @param windSpeedKmh        wind speed in km/h
 * @param isFallback          true when this context was synthesised because
 *                            the weather API was unavailable
 */
public record WeatherContext(
        double temperatureCelsius,
        double precipitationMm,
        int weatherCode,
        double windSpeedKmh,
        boolean isFallback
) {

    /**
     * Fallback context used when the weather API is unavailable.
     * Represents mild, clear-weather conditions.
     * Scored conservatively by the engine (neutral, not optimal).
     */
    public static WeatherContext fallback() {
        return new WeatherContext(20.0, 0.0, 0, 0.0, true);
    }

    /**
     * Returns true if it is currently raining or there are showers.
     * WMO codes 51–67 (drizzle/rain) and 80–99 (showers/thunderstorm).
     */
    public boolean isRaining() {
        return (weatherCode >= 51 && weatherCode <= 67) || (weatherCode >= 80 && weatherCode <= 99);
    }

    /**
     * Returns true if it is snowing.
     * WMO codes 71–77.
     */
    public boolean isSnowing() {
        return weatherCode >= 71 && weatherCode <= 77;
    }

    /**
     * Returns a human-readable description of the weather condition.
     */
    public String conditionDescription() {
        if (isFallback) return "Unknown (weather service unavailable)";
        if (weatherCode == 0) return "Clear sky";
        if (weatherCode <= 3) return "Partly cloudy";
        if (weatherCode <= 48) return "Foggy";
        if (weatherCode <= 67) return "Rainy";
        if (weatherCode <= 77) return "Snowy";
        if (weatherCode <= 99) return "Stormy / heavy showers";
        return "Unknown";
    }
}
