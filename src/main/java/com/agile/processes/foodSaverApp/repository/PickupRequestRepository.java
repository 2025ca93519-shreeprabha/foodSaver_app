package com.agile.processes.foodSaverApp.repository;

import com.agile.processes.foodSaverApp.entities.PickupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PickupRequestRepository extends JpaRepository<PickupRequest, Long> {
    List<PickupRequest> findByNgoId(Long ngoId);
    List<PickupRequest> findByFoodRestaurantId(Long restaurantId);
}
