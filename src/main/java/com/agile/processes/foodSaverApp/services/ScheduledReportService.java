package com.agile.processes.foodSaverApp.services;

import com.agile.processes.foodSaverApp.dtos.NGOAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.NGOResponseDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantAnalyticsDTO;
import com.agile.processes.foodSaverApp.dtos.RestaurantResponseDTO;
import com.agile.processes.foodSaverApp.entities.User;
import com.agile.processes.foodSaverApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledReportService {

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private NGOService ngoService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PdfGeneratorService pdfGeneratorService;

    @Autowired
    private EmailService emailService;

    // Runs on the 1st of every month at 1:00 AM
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateAndSendMonthlyReports() {
        System.out.println("Starting monthly scheduled PDF reports generation...");

        // Process Restaurants
        List<RestaurantResponseDTO> restaurants = restaurantService.getAllRestaurants();
        for (RestaurantResponseDTO restaurant : restaurants) {
            try {
                Optional<User> userOpt = userRepository.findById(restaurant.getUserId());
                if (userOpt.isPresent()) {
                    String email = userOpt.get().getEmail();
                    if (email != null && !email.isEmpty()) {
                        RestaurantAnalyticsDTO analytics = analyticsService.getRestaurantAnalytics(restaurant.getId());
                        byte[] pdfBytes = pdfGeneratorService.generateRestaurantAnalyticsPdf(analytics);

                        String month = LocalDate.now().minusMonths(1).getMonth().toString();
                        String subject = "Your Monthly Analytics Report - " + month;
                        String body = "Hello " + restaurant.getName() + ",\n\nPlease find attached your analytics report for " + month + ".\n\nThank you for using FoodSaver App!";
                        String fileName = "Restaurant_Analytics_" + month + ".pdf";

                        emailService.sendEmailWithAttachment(email, subject, body, pdfBytes, fileName);
                        System.out.println("Sent report to restaurant: " + restaurant.getName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error generating report for restaurant ID: " + restaurant.getId());
                e.printStackTrace();
            }
        }

        // Process NGOs
        List<NGOResponseDTO> ngos = ngoService.getAllNgos();
        for (NGOResponseDTO ngo : ngos) {
            try {
                Optional<User> userOpt = userRepository.findById(ngo.getUserId());
                if (userOpt.isPresent()) {
                    String email = userOpt.get().getEmail();
                    if (email != null && !email.isEmpty()) {
                        NGOAnalyticsDTO analytics = analyticsService.getNGOAnalytics(ngo.getId());
                        byte[] pdfBytes = pdfGeneratorService.generateNGOAnalyticsPdf(analytics);

                        String month = LocalDate.now().minusMonths(1).getMonth().toString();
                        String subject = "Your Monthly Analytics Report - " + month;
                        String body = "Hello " + ngo.getName() + ",\n\nPlease find attached your analytics report for " + month + ".\n\nThank you for using FoodSaver App!";
                        String fileName = "NGO_Analytics_" + month + ".pdf";

                        emailService.sendEmailWithAttachment(email, subject, body, pdfBytes, fileName);
                        System.out.println("Sent report to NGO: " + ngo.getName());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error generating report for NGO ID: " + ngo.getId());
                e.printStackTrace();
            }
        }
        
        System.out.println("Finished monthly scheduled PDF reports generation.");
    }
}
