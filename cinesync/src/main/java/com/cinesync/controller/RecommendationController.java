package com.cinesync.controller;

import com.cinesync.recommendation.RecommendationResult;
import com.cinesync.service.RecommendationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * MVC controller for the /recommendations Thymeleaf page.
 *
 * GET /recommendations
 *   – With lat/lon params: runs recommendation engine and displays results.
 *   – Without params: shows the location input form.
 *
 * Security: /recommendations/** is authenticated in SecurityConfig.
 * User identity comes from the Spring Security principal only.
 */
@Controller
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public String recommendationsPage(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lon,
            Authentication authentication,
            Model model) {

        model.addAttribute("hasResults", false);
        model.addAttribute("weatherFallback", false);
        model.addAttribute("lat", lat);
        model.addAttribute("lon", lon);

        if (lat != null && lon != null) {
            try {
                String username = authentication.getName();
                List<RecommendationResult> results = recommendationService.recommend(username, lat, lon);
                model.addAttribute("recommendations", results);
                model.addAttribute("hasResults", true);
                model.addAttribute("resultCount", results.size());

                if (!results.isEmpty()) {
                    RecommendationResult first = results.get(0);
                    boolean weatherFallback = first.weatherContext().isFallback();
                    model.addAttribute("weatherFallback", weatherFallback);
                    if (!weatherFallback) {
                        model.addAttribute("weatherCondition",
                                first.weatherContext().conditionDescription());
                        model.addAttribute("temperature",
                                first.weatherContext().temperatureCelsius());
                    }
                }
            } catch (Exception e) {
                model.addAttribute("error", "Could not generate recommendations: " + e.getMessage());
            }
        }

        return "recommendations";
    }
}
