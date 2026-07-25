package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.PickupRequestDTO;
import com.agile.processes.foodSaverApp.dtos.PickupResponseDTO;
import com.agile.processes.foodSaverApp.entities.Food;
import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.PickupRequest;
import com.agile.processes.foodSaverApp.enums.FoodStatus;
import com.agile.processes.foodSaverApp.enums.PickupStatus;
import com.agile.processes.foodSaverApp.repository.FoodRepository;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.PickupRequestRepository;
import com.agile.processes.foodSaverApp.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PickupService {

    @Autowired
    private PickupRequestRepository pickupRequestRepository;

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private NotificationService notificationService;

    /**
     * Creates a pickup request for an NGO selecting a specific food item and quantity.
     * Validates that:
     * - The NGO exists
     * - The food exists and is AVAILABLE
     * - The requested quantity does not exceed the available quantity
     * Marks the food as CLAIMED once the full quantity is reserved.
     */
    @Transactional
    public PickupResponseDTO requestPickup(PickupRequestDTO request) {
        NGO ngo = ngoRepository.findById(request.getNgoId())
                .orElseThrow(() -> new IllegalArgumentException("NGO not found with id: " + request.getNgoId()));

        Food food = foodRepository.findById(request.getFoodId())
                .orElseThrow(() -> new IllegalArgumentException("Food item not found with id: " + request.getFoodId()));

        if (food.getStatus() != FoodStatus.AVAILABLE) {
            throw new IllegalArgumentException("Food item is no longer available for pickup");
        }

        if (request.getRequestedQuantity() > food.getQuantity()) {
            throw new IllegalArgumentException(
                    "Requested quantity (" + request.getRequestedQuantity() +
                    ") exceeds available quantity (" + food.getQuantity() + " " + food.getQuantityUnit() + ")"
            );
        }

        // Deduct the requested quantity; mark CLAIMED if fully taken
        int remaining = food.getQuantity() - request.getRequestedQuantity();
        food.setQuantity(remaining);
        if (remaining == 0) {
            food.setStatus(FoodStatus.CLAIMED);
        }
        foodRepository.save(food);

        PickupRequest pickupRequest = new PickupRequest();
        pickupRequest.setNgo(ngo);
        pickupRequest.setFood(food);
        pickupRequest.setRequestedQuantity(request.getRequestedQuantity());
        pickupRequest.setStatus(PickupStatus.PENDING);

        PickupRequest saved = pickupRequestRepository.save(pickupRequest);

        // Notify the restaurant about the pickup request
        String message = String.format("NGO %s requested a pickup of %d %s of %s.",
                ngo.getName(),
                request.getRequestedQuantity(),
                food.getQuantityUnit(),
                food.getName());
        notificationService.createNotification(food.getRestaurant(), message);

        return mapPickupToDTO(saved);
    }

    /**
     * Returns all pickup requests submitted by a specific NGO.
     */
    @Transactional(readOnly = true)
    public List<PickupResponseDTO> getPickupsByNgo(Long ngoId) {
        if (!ngoRepository.existsById(ngoId)) {
            throw new IllegalArgumentException("NGO not found with id: " + ngoId);
        }
        return pickupRequestRepository.findByNgoId(ngoId).stream()
                .map(this::mapPickupToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns all pickup requests for food items belonging to a specific restaurant.
     */
    @Transactional(readOnly = true)
    public List<PickupResponseDTO> getPickupsByRestaurant(Long restaurantId) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found with id: " + restaurantId);
        }
        return pickupRequestRepository.findByFoodRestaurantId(restaurantId).stream()
                .map(this::mapPickupToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a pickup request by the Restaurant.
     * Validates that the pickup request belongs to the restaurant's food.
     * If the new status is CANCELLED:
     * - Restores the requested quantity back to the Food item.
     * - Reverts the Food status to AVAILABLE if it was CLAIMED.
     */
    @Transactional
    public PickupResponseDTO updateStatusByRestaurant(Long restaurantId, Long pickupId, PickupStatus status) {
        if (!restaurantRepository.existsById(restaurantId)) {
            throw new IllegalArgumentException("Restaurant not found with id: " + restaurantId);
        }

        PickupRequest pickupRequest = pickupRequestRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup request not found with id: " + pickupId));

        if (!pickupRequest.getFood().getRestaurant().getId().equals(restaurantId)) {
            throw new IllegalArgumentException("Pickup request does not belong to this restaurant");
        }

        if (pickupRequest.getStatus() != PickupStatus.PENDING) {
            throw new IllegalArgumentException("Cannot update status of a " + pickupRequest.getStatus() + " pickup request");
        }

        if (status != PickupStatus.COMPLETED && status != PickupStatus.CANCELLED) {
            throw new IllegalArgumentException("Invalid status update for restaurant. Must be COMPLETED or CANCELLED");
        }

        pickupRequest.setStatus(status);

        if (status == PickupStatus.CANCELLED) {
            Food food = pickupRequest.getFood();
            food.setQuantity(food.getQuantity() + pickupRequest.getRequestedQuantity());
            if (food.getStatus() == FoodStatus.CLAIMED) {
                food.setStatus(FoodStatus.AVAILABLE);
            }
            foodRepository.save(food);
        }

        PickupRequest saved = pickupRequestRepository.save(pickupRequest);
        return mapPickupToDTO(saved);
    }

    /**
     * Updates the status of a pickup request by the NGO.
     * Validates that the pickup request belongs to the NGO.
     * Only allows setting the status to CANCELLED.
     * Restores the requested quantity back to the Food item.
     * Reverts the Food status to AVAILABLE if it was CLAIMED.
     * Notifies the restaurant.
     */
    @Transactional
    public PickupResponseDTO updateStatusByNGO(Long ngoId, Long pickupId, PickupStatus status) {
        if (!ngoRepository.existsById(ngoId)) {
            throw new IllegalArgumentException("NGO not found with id: " + ngoId);
        }

        PickupRequest pickupRequest = pickupRequestRepository.findById(pickupId)
                .orElseThrow(() -> new IllegalArgumentException("Pickup request not found with id: " + pickupId));

        if (!pickupRequest.getNgo().getId().equals(ngoId)) {
            throw new IllegalArgumentException("Pickup request does not belong to this NGO");
        }

        if (pickupRequest.getStatus() != PickupStatus.PENDING) {
            throw new IllegalArgumentException("Cannot update status of a " + pickupRequest.getStatus() + " pickup request");
        }

        if (status != PickupStatus.CANCELLED) {
            throw new IllegalArgumentException("NGO can only cancel a pickup request");
        }

        pickupRequest.setStatus(status);

        Food food = pickupRequest.getFood();
        food.setQuantity(food.getQuantity() + pickupRequest.getRequestedQuantity());
        if (food.getStatus() == FoodStatus.CLAIMED) {
            food.setStatus(FoodStatus.AVAILABLE);
        }
        foodRepository.save(food);

        // Notify Restaurant
        String message = String.format("NGO %s cancelled the pickup request for %s.",
                pickupRequest.getNgo().getName(),
                food.getName());
        notificationService.createNotification(food.getRestaurant(), message);

        PickupRequest saved = pickupRequestRepository.save(pickupRequest);
        return mapPickupToDTO(saved);
    }

    private PickupResponseDTO mapPickupToDTO(PickupRequest pr) {
        return new PickupResponseDTO(
                pr.getId(),
                pr.getNgo().getId(),
                pr.getNgo().getName(),
                pr.getFood().getId(),
                pr.getFood().getName(),
                pr.getFood().getRestaurant().getName(),
                pr.getRequestedQuantity(),
                pr.getFood().getQuantityUnit(),
                pr.getStatus(),
                pr.getCreatedAt()
        );
    }
}
