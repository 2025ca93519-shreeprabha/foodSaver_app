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
