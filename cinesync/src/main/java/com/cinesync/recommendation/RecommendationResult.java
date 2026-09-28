package com.cinesync.recommendation;

import com.cinesync.dto.MovieView;
import com.cinesync.dto.ShowView;
import com.cinesync.dto.TheatreView;

import java.util.List;

/**
 * Immutable DTO representing a single recommendation result.
 *
 * Scores are expressed as 0–100 within each component's weighted range.
 *
 * Weight table:
 *   Rating         25%
 *   Genre Match    25%
 *   Theatre Dist   20%
 *   Weather        15%
 *   Showtime       15%
 *   ─────────────────
 *   Total         100%
 *
 * experienceScore = ratingScore*0.25 + genreScore*0.25 + distanceScore*0.20
 *                 + weatherScore*0.15 + showtimeScore*0.15
 */
public record RecommendationResult(
        MovieView movie,
        ShowView show,
        TheatreView theatre,

        /** Final weighted score 0–100 */
        double experienceScore,

        /** Rating component raw score 0–100 */
        double ratingScore,

        /** Genre match component raw score 0–100 */
        double genreScore,

        /** Distance component raw score 0–100 */
        double distanceScore,

        /** Weather suitability component raw score 0–100 */
        double weatherScore,

        /** Showtime preference match component raw score 0–100 */
        double showtimeScore,

        /** Haversine distance to theatre in kilometres */
        double distanceKm,

        /** Weather context used in this calculation */
        WeatherContext weatherContext,

        /** Human-readable explanation lines for the UI */
        List<String> reasons
) { }
