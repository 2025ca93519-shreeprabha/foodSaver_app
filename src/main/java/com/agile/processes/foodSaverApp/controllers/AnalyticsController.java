package com.agile.processes.foodSaverApp.controllers;

import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.services.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    /**
     * Retrieves analytics for a specific restaurant.
     * GET /restaurants/{restaurantId}/analytics
     */
    @GetMapping("/restaurants/{restaurantId}/analytics")
    public ResponseEntity<RestaurantAnalyticsDTO> getRestaurantAnalytics(@PathVariable Long restaurantId) {
        RestaurantAnalyticsDTO analytics = analyticsService.getRestaurantAnalytics(restaurantId);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Retrieves analytics for a specific NGO.
     * GET /ngo/{ngoId}/analytics
     */
    @GetMapping("/ngo/{ngoId}/analytics")
    public ResponseEntity<NGOAnalyticsDTO> getNGOAnalytics(@PathVariable Long ngoId) {
        NGOAnalyticsDTO analytics = analyticsService.getNGOAnalytics(ngoId);
        return ResponseEntity.ok(analytics);
    }
}
