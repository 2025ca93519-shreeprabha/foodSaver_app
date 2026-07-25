package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.PickupRequest;
import com.agile.processes.foodSaverApp.entities.Restaurant;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.PickupStatus;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.PickupRequestRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private PickupRequestRepository pickupRequestRepository;

    /**
     * Calculates analytics for a given Restaurant.
     */
    @Transactional(readOnly = true)
    public RestaurantAnalyticsDTO getRestaurantAnalytics(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found with id: " + restaurantId));

        List<Food> foods = foodRepository.findByRestaurantId(restaurantId);
        List<PickupRequest> pickups = pickupRequestRepository.findByFoodRestaurantId(restaurantId);

        LocalDateTime now = LocalDateTime.now();

        // Calculate counts
        long totalDonationsCount = foods.size();
        long activeDonationsCount = 0;
        long claimedDonationsCount = 0;
        long expiredDonationsCount = 0;

        for (Food food : foods) {
            if (food.getStatus() == FoodStatus.AVAILABLE && food.getQuantity() > 0) {
                activeDonationsCount++;
            }
            if (food.getStatus() == FoodStatus.CLAIMED) {
                claimedDonationsCount++;
            }
            if (food.getStatus() == FoodStatus.EXPIRED || food.getExpiryTime().isBefore(now)) {
                expiredDonationsCount++;
            }
        }

        // Group quantities by unit
        Map<String, Integer> totalQuantityPostedByUnit = new HashMap<>();
        Map<String, Integer> totalQuantityClaimedByUnit = new HashMap<>();
        Map<String, Integer> totalQuantityCompletedByUnit = new HashMap<>();

        // Add remaining/available food quantities to posted
        for (Food food : foods) {
            String unit = food.getQuantityUnit().toLowerCase();
            totalQuantityPostedByUnit.put(unit, totalQuantityPostedByUnit.getOrDefault(unit, 0) + food.getQuantity());
        }

        long pendingPickupsCount = 0;
        long completedPickupsCount = 0;
        long cancelledPickupsCount = 0;

        for (PickupRequest pr : pickups) {
            String unit = pr.getFood().getQuantityUnit().toLowerCase();
            int qty = pr.getRequestedQuantity();

            if (pr.getStatus() == PickupStatus.PENDING) {
                pendingPickupsCount++;
                totalQuantityClaimedByUnit.put(unit, totalQuantityClaimedByUnit.getOrDefault(unit, 0) + qty);
                totalQuantityPostedByUnit.put(unit, totalQuantityPostedByUnit.getOrDefault(unit, 0) + qty);
            } else if (pr.getStatus() == PickupStatus.COMPLETED) {
                completedPickupsCount++;
                totalQuantityClaimedByUnit.put(unit, totalQuantityClaimedByUnit.getOrDefault(unit, 0) + qty);
                totalQuantityCompletedByUnit.put(unit, totalQuantityCompletedByUnit.getOrDefault(unit, 0) + qty);
                totalQuantityPostedByUnit.put(unit, totalQuantityPostedByUnit.getOrDefault(unit, 0) + qty);
            } else if (pr.getStatus() == PickupStatus.CANCELLED) {
                cancelledPickupsCount++;
            }
        }

        long totalPickupsReceived = pickups.size();

        return new RestaurantAnalyticsDTO(
                restaurantId,
                restaurant.getName(),
                totalDonationsCount,
                activeDonationsCount,
                claimedDonationsCount,
                expiredDonationsCount,
                totalQuantityPostedByUnit,
                totalQuantityClaimedByUnit,
                totalQuantityCompletedByUnit,
                totalPickupsReceived,
                pendingPickupsCount,
                completedPickupsCount,
                cancelledPickupsCount
        );
    }

    /**
     * Calculates analytics for a given NGO.
     */
    @Transactional(readOnly = true)
    public NGOAnalyticsDTO getNGOAnalytics(Long ngoId) {
        NGO ngo = ngoRepository.findById(ngoId)
                .orElseThrow(() -> new IllegalArgumentException("NGO not found with id: " + ngoId));

        List<PickupRequest> pickups = pickupRequestRepository.findByNgoId(ngoId);

        long totalPickupsRequested = pickups.size();
        long pendingPickupsCount = 0;
        long completedPickupsCount = 0;
        long cancelledPickupsCount = 0;

        Map<String, Integer> totalQuantityClaimedByUnit = new HashMap<>();
        Map<String, Integer> totalQuantityCompletedByUnit = new HashMap<>();

        for (PickupRequest pr : pickups) {
            String unit = pr.getFood().getQuantityUnit().toLowerCase();
            int qty = pr.getRequestedQuantity();

            if (pr.getStatus() == PickupStatus.PENDING) {
                pendingPickupsCount++;
                totalQuantityClaimedByUnit.put(unit, totalQuantityClaimedByUnit.getOrDefault(unit, 0) + qty);
            } else if (pr.getStatus() == PickupStatus.COMPLETED) {
                completedPickupsCount++;
                totalQuantityClaimedByUnit.put(unit, totalQuantityClaimedByUnit.getOrDefault(unit, 0) + qty);
                totalQuantityCompletedByUnit.put(unit, totalQuantityCompletedByUnit.getOrDefault(unit, 0) + qty);
            } else if (pr.getStatus() == PickupStatus.CANCELLED) {
                cancelledPickupsCount++;
            }
        }

        return new NGOAnalyticsDTO(
                ngoId,
                ngo.getName(),
                totalPickupsRequested,
                pendingPickupsCount,
                completedPickupsCount,
                cancelledPickupsCount,
                totalQuantityClaimedByUnit,
                totalQuantityCompletedByUnit
        );
    }
}
