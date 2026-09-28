package com.cinesync.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * External DTO for a single geocoding result from the Nominatim API.
 * Used when the user provides a place name instead of lat/lon coordinates.
 * This is NOT a JPA entity.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NominatimResult {

    @JsonProperty("lat")
    private String lat;

    @JsonProperty("lon")
    private String lon;

    @JsonProperty("display_name")
    private String displayName;

    public String getLat() { return lat; }
    public String getLon() { return lon; }
    public String getDisplayName() { return displayName; }
}
