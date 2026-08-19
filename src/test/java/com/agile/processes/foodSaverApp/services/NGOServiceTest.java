package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGORegisterRequestDTO;
import com.agile.processes.foodSaverApp.dtos.NGOResponseDTO;
import com.agile.processes.foodSaverApp.entities.NGO;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.enums.Role;
import com.agile.processes.foodSaverApp.repository.NGORepository;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NGOServiceTest {

    @Mock
    private NGORepository ngoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NGOService ngoService;

    private User testUser;
    private NGORegisterRequestDTO registerRequest;
    private NGO testNgo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setRole(Role.NGO);

        registerRequest = new NGORegisterRequestDTO();
        registerRequest.setUserId(1L);
        registerRequest.setName("Test NGO");
        registerRequest.setAddress("123 NGO Street");
        registerRequest.setPhone("1234567890");

        testNgo = new NGO();
        testNgo.setId(10L);
        testNgo.setUser(testUser);
        testNgo.setName("Test NGO");
        testNgo.setAddress("123 NGO Street");
        testNgo.setPhone("1234567890");
    }

    @Test
    void registerNGO_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(ngoRepository.existsByUser(any(User.class))).thenReturn(false);
        when(ngoRepository.save(any(NGO.class))).thenReturn(testNgo);

        NGO savedNgo = ngoService.registerNGO(registerRequest);

        assertNotNull(savedNgo);
        assertEquals("Test NGO", savedNgo.getName());
        verify(ngoRepository, times(1)).save(any(NGO.class));
    }

    @Test
    void registerNGO_UserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ngoService.registerNGO(registerRequest);
        });

        assertEquals("User not found", exception.getMessage());
        verify(ngoRepository, never()).save(any(NGO.class));
    }

    @Test
    void registerNGO_UserNotNgoRole() {
        testUser.setRole(Role.RESTAURANT);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ngoService.registerNGO(registerRequest);
        });

        assertEquals("User does not have NGO role", exception.getMessage());
        verify(ngoRepository, never()).save(any(NGO.class));
    }

    @Test
    void registerNGO_AlreadyRegistered() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(ngoRepository.existsByUser(any(User.class))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ngoService.registerNGO(registerRequest);
        });

        assertEquals("NGO is already registered for this user", exception.getMessage());
        verify(ngoRepository, never()).save(any(NGO.class));
    }

    @Test
    void getAllNgos_Success() {
        NGO anotherNgo = new NGO();
        anotherNgo.setId(20L);
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherNgo.setUser(anotherUser);
        
        when(ngoRepository.findAll()).thenReturn(Arrays.asList(testNgo, anotherNgo));

        List<NGOResponseDTO> ngoResponses = ngoService.getAllNgos();

        assertNotNull(ngoResponses);
        assertEquals(2, ngoResponses.size());
        assertEquals(10L, ngoResponses.get(0).getId());
        assertEquals(20L, ngoResponses.get(1).getId());
    }
}
