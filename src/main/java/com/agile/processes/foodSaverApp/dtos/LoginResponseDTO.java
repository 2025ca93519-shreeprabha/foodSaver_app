package com.agile.processes.foodSaverApp.dtos;

import com.agile.processes.foodSaverApp.enums.Role;
import lombok.Data;

@Data
public class LoginResponseDTO {
    private String message;
    private String name;
    private String email;
    private Role role;

    public LoginResponseDTO(String message, String name, String email, Role role) {
        this.message = message;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
