package com.cinesync.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configures the RestTemplate used by all external API clients.
 *
 * A single named bean is shared between MovieApiClient, WeatherApiClient,
 * and GeocodingApiClient. Timeouts prevent hung threads when external APIs
 * are slow or unreachable.
 *
 * Configuration (application.properties / environment):
 *   external.api.connect-timeout-ms  – default 5000 ms
 *   external.api.read-timeout-ms     – default 8000 ms
 */
@Configuration
public class ExternalApiConfig {

    @Bean
    public RestTemplate externalApiRestTemplate(
            @Value("${external.api.connect-timeout-ms:5000}") int connectTimeoutMs,
            @Value("${external.api.read-timeout-ms:8000}") int readTimeoutMs) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);

        RestTemplate restTemplate = new RestTemplate(factory);

        // Add a Nominatim-compliant User-Agent for all requests.
        // Nominatim policy requires a descriptive User-Agent.
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("User-Agent", "CineSync/1.0 (Movie Recommendation System)");
            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
