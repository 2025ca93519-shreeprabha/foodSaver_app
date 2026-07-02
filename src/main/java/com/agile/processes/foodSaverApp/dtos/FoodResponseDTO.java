package com.agile.processes.foodSaverApp.dtos;

import com.agile.processes.foodSaverApp.enums.FoodStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FoodResponseDTO {
    private Long id;
    private Long restaurantId;
    private String restaurantName;
    private String name;
    private String description;
    private Integer quantity;
    private String quantityUnit;
    private LocalDateTime expiryTime;
    private FoodStatus status;
    private LocalDateTime createdAt;
}
