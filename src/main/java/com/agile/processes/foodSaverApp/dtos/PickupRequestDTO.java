package com.agile.processes.foodSaverApp.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PickupRequestDTO {

    @NotNull(message = "NGO ID is required")
    private Long ngoId;

    @NotNull(message = "Food ID is required")
    private Long foodId;

    @NotNull(message = "Requested quantity is required")
    @Min(value = 1, message = "Requested quantity must be at least 1")
    private Integer requestedQuantity;
}
