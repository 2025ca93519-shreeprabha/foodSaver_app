package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PdfGeneratorServiceTest {

    @InjectMocks
    private PdfGeneratorService pdfGeneratorService;

    private RestaurantAnalyticsDTO restaurantAnalytics;
    private NGOAnalyticsDTO ngoAnalytics;

    @BeforeEach
    void setUp() {
        Map<String, Integer> mapData = new HashMap<>();
        mapData.put("kg", 10);
        mapData.put("liters", 5);

        restaurantAnalytics = new RestaurantAnalyticsDTO(
                1L, "Test Restaurant", 10, 5, 2, 3,
                mapData, mapData, mapData,
                20, 10, 5, 5
        );

        ngoAnalytics = new NGOAnalyticsDTO(
                2L, "Test NGO",
                20, 10, 5, 5,
                mapData, mapData
        );
    }

    @Test
    void generateRestaurantAnalyticsPdf_Success() {
        byte[] pdfBytes = pdfGeneratorService.generateRestaurantAnalyticsPdf(restaurantAnalytics);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateRestaurantAnalyticsPdf_NullData() {
        restaurantAnalytics.setTotalQuantityPostedByUnit(null); // Testing with null map data
        byte[] pdfBytes = pdfGeneratorService.generateRestaurantAnalyticsPdf(restaurantAnalytics);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateNGOAnalyticsPdf_Success() {
        byte[] pdfBytes = pdfGeneratorService.generateNGOAnalyticsPdf(ngoAnalytics);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }

    @Test
    void generateNGOAnalyticsPdf_EmptyData() {
        ngoAnalytics.setTotalQuantityClaimedByUnit(new HashMap<>()); // Testing with empty map data
        byte[] pdfBytes = pdfGeneratorService.generateNGOAnalyticsPdf(ngoAnalytics);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
    }
}
