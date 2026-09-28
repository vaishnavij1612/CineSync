package com.cinesync.client;

import com.cinesync.client.dto.NominatimResult;
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
 * HTTP client for the Nominatim geocoding API (OpenStreetMap).
 *
 * Usage policy: https://operations.osmfoundation.org/policies/nominatim/
 *   - Must provide a descriptive User-Agent (set in ExternalApiConfig)
 *   - Must not send bulk/automated requests
 *
 * Configuration:
 *   geocoding.base-url – defaults to https://nominatim.openstreetmap.org
 *
 * Used ONLY when the user provides a place name instead of lat/lon.
 * If lat/lon are already supplied, this client is NOT called.
 * Returns Optional.empty() on any failure – recommendations continue
 * if coordinates were already provided.
 */
@Component
public class GeocodingApiClient {

    private static final Logger log = LoggerFactory.getLogger(GeocodingApiClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public GeocodingApiClient(
            RestTemplate externalApiRestTemplate,
            @Value("${geocoding.base-url:https://nominatim.openstreetmap.org}") String baseUrl) {
        this.restTemplate = externalApiRestTemplate;
        this.baseUrl = baseUrl;
    }

    /**
     * Geocodes a place name to latitude/longitude coordinates.
     *
     * @param placeName city name, address, or other place description
     * @return Optional double array [latitude, longitude], or empty on failure
     */
    public Optional<double[]> geocode(String placeName) {
        if (placeName == null || placeName.isBlank()) {
            return Optional.empty();
        }
        try {
            String url = UriComponentsBuilder.fromUriString(baseUrl + "/search")
                    .queryParam("q", placeName)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .build()
                    .toUriString();

            NominatimResult[] results = restTemplate.getForObject(url, NominatimResult[].class);
            if (results == null || results.length == 0) {
                log.warn("Nominatim returned no results for place: '{}'", placeName);
                return Optional.empty();
            }

            NominatimResult first = results[0];
            double lat = Double.parseDouble(first.getLat());
            double lon = Double.parseDouble(first.getLon());
            log.debug("Geocoded '{}' → lat={} lon={} ({})", placeName, lat, lon, first.getDisplayName());
            return Optional.of(new double[]{lat, lon});

        } catch (ResourceAccessException e) {
            log.warn("Nominatim is unreachable: {}", e.getMessage());
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("Nominatim returned an error response for '{}': {}", placeName, e.getMessage());
            return Optional.empty();
        } catch (NumberFormatException e) {
            log.warn("Nominatim returned unparseable coordinates: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
