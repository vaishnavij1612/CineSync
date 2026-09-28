package com.cinesync.recommendation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link HaversineCalculator}.
 * All tests are deterministic with no external dependencies.
 */
class HaversineCalculatorTest {

    @Test
    void samePointReturnsZeroDistance() {
        double dist = HaversineCalculator.distanceKm(12.9716, 77.5946, 12.9716, 77.5946);
        assertEquals(0.0, dist, 0.001);
    }

    @Test
    void knownDistanceBetweenBangalorePoints() {
        // Downtown Screens (12.9716, 77.5946) to Riverside Cinema (12.9352, 77.6245)
        // Expected approximately 4.9 km
        double dist = HaversineCalculator.distanceKm(12.9716, 77.5946, 12.9352, 77.6245);
        assertTrue(dist > 4.0 && dist < 6.0,
                "Expected ~4.9 km between Bangalore test points, got: " + dist);
    }

    @Test
    void distanceFromLondonToParisIsApproximately340km() {
        // London (51.5074, -0.1278) → Paris (48.8566, 2.3522)
        double dist = HaversineCalculator.distanceKm(51.5074, -0.1278, 48.8566, 2.3522);
        assertTrue(dist > 330 && dist < 350,
                "Expected ~340 km London to Paris, got: " + dist);
    }

    @Test
    void distanceIsSymmetric() {
        double a = HaversineCalculator.distanceKm(12.9716, 77.5946, 12.9352, 77.6245);
        double b = HaversineCalculator.distanceKm(12.9352, 77.6245, 12.9716, 77.5946);
        assertEquals(a, b, 0.0001, "Haversine distance should be symmetric");
    }

    @Test
    void distanceIsAlwaysNonNegative() {
        double dist = HaversineCalculator.distanceKm(0, 0, 0, 0);
        assertTrue(dist >= 0.0);
    }
}
