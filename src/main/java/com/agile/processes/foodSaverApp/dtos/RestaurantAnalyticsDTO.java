package com.agile.processes.foodSaverApp.dtos;

import com.agile.processes.foodSaverApp.enums.FoodStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantAnalyticsDTO {
    private Long restaurantId;
    private String restaurantName;
    private long totalDonationsCount;
    private long activeDonationsCount;
    private long claimedDonationsCount;
    private long expiredDonationsCount;
    private Map<String, Integer> totalQuantityPostedByUnit;
    private Map<String, Integer> totalQuantityClaimedByUnit;
    private Map<String, Integer> totalQuantityCompletedByUnit;
    private long totalPickupsReceived;
    private long pendingPickupsCount;
    private long completedPickupsCount;
    private long cancelledPickupsCount;
}
