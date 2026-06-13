package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGORegisterRequestDTO;
import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NGOService {

    @Autowired
    private NGORepository ngoRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public NGO registerNGO(NGORegisterRequestDTO request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() != Role.NGO) {
            throw new IllegalArgumentException("User does not have NGO role");
        }

        if (ngoRepository.existsByUser(user)) {
            throw new IllegalArgumentException("NGO is already registered for this user");
        }

        NGO ngo = new NGO();
        ngo.setUser(user);
        ngo.setName(request.getName());
        ngo.setAddress(request.getAddress());
        ngo.setPhone(request.getPhone());

        return ngoRepository.save(ngo);
    }
}
