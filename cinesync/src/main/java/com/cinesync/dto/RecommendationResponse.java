package com.cinesync.dto;

import com.cinesync.recommendation.RecommendationResult;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST response DTO for a single recommendation.
 * Returned by GET /api/recommendations as part of a list.
 *
 * This record is safe for JSON serialisation – it contains only
 * primitives, strings, and nested records.
 */
public record RecommendationResponse(
        Long showId,
        String movieTitle,
        String movieOverview,
        Double movieRating,
        String posterUrl,
        List<String> genres,

        String theatreName,
        String theatreAddress,
        Double theatreLatitude,
        Double theatreLongitude,
        Double distanceKm,

        LocalDateTime showtime,
        String showtimeCategory,

        double experienceScore,
        double ratingScore,
        double genreScore,
        double distanceScore,
        double weatherScore,
        double showtimeScore,

        String weatherCondition,
        double temperatureCelsius,
        double precipitationMm,
        boolean weatherIsFallback,

        List<String> reasons
) {

    /**
     * Maps a domain RecommendationResult to this REST response DTO.
     */
    public static RecommendationResponse from(RecommendationResult result) {
        return new RecommendationResponse(
                result.show().id(),
                result.movie().title(),
                result.movie().overview(),
                result.movie().rating(),
                result.movie().posterUrl(),
                result.movie().genres(),

                result.theatre().name(),
                result.theatre().address(),
                result.theatre().latitude(),
                result.theatre().longitude(),
                Math.round(result.distanceKm() * 10.0) / 10.0,

                result.show().showtime(),
                result.weatherContext() != null ? "computed" : "unknown",

                result.experienceScore(),
                Math.round(result.ratingScore() * 10.0) / 10.0,
                Math.round(result.genreScore() * 10.0) / 10.0,
                Math.round(result.distanceScore() * 10.0) / 10.0,
                Math.round(result.weatherScore() * 10.0) / 10.0,
                Math.round(result.showtimeScore() * 10.0) / 10.0,

                result.weatherContext() != null ? result.weatherContext().conditionDescription() : "Unknown",
                result.weatherContext() != null ? result.weatherContext().temperatureCelsius() : 0.0,
                result.weatherContext() != null ? result.weatherContext().precipitationMm() : 0.0,
                result.weatherContext() != null && result.weatherContext().isFallback(),

                result.reasons()
        );
    }
}
