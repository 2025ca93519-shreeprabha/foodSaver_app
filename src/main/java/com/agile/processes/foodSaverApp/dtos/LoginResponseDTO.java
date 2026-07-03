package com.agile.processes.foodSaverApp.dtos;

import com.agile.processes.foodSaverApp.enums.Role;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private Long userId;
    private Long restaurantId;
    private Long ngoId;
    private String message;
    private String name;
    private String email;
    private Role role;

    public LoginResponseDTO(Long userId, String message, String name, String email, Role role, Long restaurantId, Long ngoId) {
        this.userId = userId;
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
        this.restaurantId = restaurantId;
        this.ngoId = ngoId;
    }
}
