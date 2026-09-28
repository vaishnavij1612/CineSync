package com.cinesync.dto;

import com.cinesync.client.dto.TmdbMovieDto;
import java.util.List;

/**
 * REST response DTO for external movie discovery results.
 * Returned by GET /api/discover/movies?query=...
 *
 * Wraps TMDB data in a CineSync-controlled structure so the raw
 * third-party API response is never exposed directly.
 */
public record MovieDiscoveryDto(
        Long externalId,
        String title,
        String overview,
        Double rating,
        String releaseDate,
        String posterUrl,
        List<Integer> genreIds
) {

    /**
     * Maps a TMDB client DTO to this discovery DTO.
     * External ID is included to allow future local import if needed.
     */
    public static MovieDiscoveryDto from(TmdbMovieDto tmdb) {
        return new MovieDiscoveryDto(
                tmdb.getExternalId(),
                tmdb.getTitle(),
                tmdb.getOverview(),
                tmdb.getVoteAverage(),
                tmdb.getReleaseDate(),
                tmdb.getPosterUrl(),
                tmdb.getGenreIds()
        );
    }
}
