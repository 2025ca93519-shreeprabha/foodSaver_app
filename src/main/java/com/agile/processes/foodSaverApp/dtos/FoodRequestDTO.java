package com.agile.processes.foodSaverApp.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FoodRequestDTO {

    @NotNull(message = "Restaurant ID is required")
    private Long restaurantId;

    @NotBlank(message = "Food name is required")
    private String name;

    private String description;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotBlank(message = "Quantity unit is required")
    private String quantityUnit;

    @NotNull(message = "Expiry time is required")
    private LocalDateTime expiryTime;
}
