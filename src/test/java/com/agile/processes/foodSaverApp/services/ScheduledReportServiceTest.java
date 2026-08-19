package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.NGOResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduledReportServiceTest {

    @Mock
    private RestaurantService restaurantService;

    @Mock
    private NGOService ngoService;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PdfGeneratorService pdfGeneratorService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ScheduledReportService scheduledReportService;

    private User testUserRestaurant;
    private User testUserNgo;
    private RestaurantResponseDTO restaurantDTO;
    private NGOResponseDTO ngoDTO;

    @BeforeEach
    void setUp() {
        testUserRestaurant = new User();
        testUserRestaurant.setId(10L);
        testUserRestaurant.setEmail("restaurant@test.com");

        testUserNgo = new User();
        testUserNgo.setId(20L);
        testUserNgo.setEmail("ngo@test.com");

        restaurantDTO = new RestaurantResponseDTO(1L, 10L, "Test Restaurant", "123 St", "1234567890", LocalDateTime.now());
        ngoDTO = new NGOResponseDTO(2L, 20L, "Test NGO", "456 Ave", "0987654321", LocalDateTime.now());
    }

    @Test
    void generateAndSendMonthlyReports_Success() throws Exception {
        when(restaurantService.getAllRestaurants()).thenReturn(Arrays.asList(restaurantDTO));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUserRestaurant));
        when(analyticsService.getRestaurantAnalytics(1L)).thenReturn(new RestaurantAnalyticsDTO());
        when(pdfGeneratorService.generateRestaurantAnalyticsPdf(any(RestaurantAnalyticsDTO.class))).thenReturn(new byte[]{1, 2, 3});

        when(ngoService.getAllNgos()).thenReturn(Arrays.asList(ngoDTO));
        when(userRepository.findById(20L)).thenReturn(Optional.of(testUserNgo));
        when(analyticsService.getNGOAnalytics(2L)).thenReturn(new NGOAnalyticsDTO());
        when(pdfGeneratorService.generateNGOAnalyticsPdf(any(NGOAnalyticsDTO.class))).thenReturn(new byte[]{4, 5, 6});

        scheduledReportService.generateAndSendMonthlyReports();

        verify(emailService, times(1)).sendEmailWithAttachment(eq("restaurant@test.com"), anyString(), anyString(), any(byte[].class), anyString());
        verify(emailService, times(1)).sendEmailWithAttachment(eq("ngo@test.com"), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    void generateAndSendMonthlyReports_UserNotFound() throws Exception {
        when(restaurantService.getAllRestaurants()).thenReturn(Arrays.asList(restaurantDTO));
        when(userRepository.findById(10L)).thenReturn(Optional.empty()); // User not found

        when(ngoService.getAllNgos()).thenReturn(Arrays.asList(ngoDTO));
        when(userRepository.findById(20L)).thenReturn(Optional.empty()); // User not found

        scheduledReportService.generateAndSendMonthlyReports();

        verify(emailService, never()).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }

    @Test
    void generateAndSendMonthlyReports_PdfExceptionHandled() throws Exception {
        when(restaurantService.getAllRestaurants()).thenReturn(Arrays.asList(restaurantDTO));
        when(userRepository.findById(10L)).thenReturn(Optional.of(testUserRestaurant));
        when(analyticsService.getRestaurantAnalytics(1L)).thenReturn(new RestaurantAnalyticsDTO());
        when(pdfGeneratorService.generateRestaurantAnalyticsPdf(any(RestaurantAnalyticsDTO.class))).thenThrow(new RuntimeException("PDF Generation failed"));

        when(ngoService.getAllNgos()).thenReturn(Arrays.asList(ngoDTO));
        when(userRepository.findById(20L)).thenReturn(Optional.of(testUserNgo));
        when(analyticsService.getNGOAnalytics(2L)).thenReturn(new NGOAnalyticsDTO());
        when(pdfGeneratorService.generateNGOAnalyticsPdf(any(NGOAnalyticsDTO.class))).thenThrow(new RuntimeException("PDF Generation failed"));

        // Should not throw exception upwards, it catches it and prints stack trace
        scheduledReportService.generateAndSendMonthlyReports();

        verify(emailService, never()).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(byte[].class), anyString());
    }
}
