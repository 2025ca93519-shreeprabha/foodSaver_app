package com.agile.processes.foodSaverApp.dtos;

import com.agile.processes.foodSaverApp.enums.PickupStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupResponseDTO {
    private Long id;
    private Long ngoId;
    private String ngoName;
    private Long foodId;
    private String foodName;
    private String restaurantName;
    private Integer requestedQuantity;
    private String quantityUnit;
    private PickupStatus status;
    private LocalDateTime createdAt;
}
