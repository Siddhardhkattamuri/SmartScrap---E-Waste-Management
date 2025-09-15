package com.example.SmartScrap.controller;

import com.example.SmartScrap.dto.PickupPersonRequestViewDto;
import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.Status;
import com.example.SmartScrap.service.PickupPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pickup-person")
public class PickupPersonController {

    @Autowired
    private PickupPersonService pickupPersonService;

    @GetMapping("/dashboard")
    public ResponseEntity<List<PickupPersonRequestViewDto>> getDashboard() {
        System.out.println("[DASHBOARD] Getting pickup person dashboard");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        System.out.println("[DASHBOARD] Authenticated user: " + email);
        
        // Get the pickup person's ID from the authentication
        // For now, we'll need to get the user ID from the email
        // In a real application, you might want to store the user ID in the JWT token
        
        // This is a simplified approach - in production, you'd want to get the user ID more efficiently
        Long pickupPersonId = getCurrentPickupPersonId(email);
        System.out.println("[DASHBOARD] Pickup person ID: " + pickupPersonId);
        
        List<PickupPersonRequestViewDto> requests = pickupPersonService.getPickupPersonRequests(pickupPersonId);
        System.out.println("[DASHBOARD] Found " + requests.size() + " requests for pickup person");
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/requests/{id}/status")
    public ResponseEntity<EwasteRequest> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody StatusUpdateRequest statusUpdate) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Long pickupPersonId = getCurrentPickupPersonId(email);
            
            EwasteRequest request = pickupPersonService.updateRequestStatus(id, pickupPersonId, statusUpdate.getStatus());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/requests/{id}/initiate-completion")
    public ResponseEntity<String> initiateCompletion(@PathVariable Long id) {
        System.out.println("[CONTROLLER] Initiate completion called for request: " + id);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            System.out.println("[CONTROLLER] Authenticated user: " + email);
            
            Long pickupPersonId = getCurrentPickupPersonId(email);
            System.out.println("[CONTROLLER] Pickup person ID: " + pickupPersonId);
            
            String message = pickupPersonService.initiateCompletion(id, pickupPersonId);
            System.out.println("[CONTROLLER] Service returned: " + message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            System.err.println("[CONTROLLER] Error in initiate completion: " + e.getMessage());
            e.printStackTrace();
            
            // Ensure we return a proper error message
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "Unknown error occurred while initiating completion";
            }
            
            System.err.println("[CONTROLLER] Returning error message: " + errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        }
    }

    @PostMapping("/requests/{id}/verify-otp")
    public ResponseEntity<EwasteRequest> verifyOtpAndComplete(
            @PathVariable Long id,
            @RequestBody OtpVerificationRequest otpRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            Long pickupPersonId = getCurrentPickupPersonId(email);
            
            EwasteRequest request = pickupPersonService.verifyOtpAndComplete(id, pickupPersonId, otpRequest.getOtp());
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    private Long getCurrentPickupPersonId(String email) {
        // This is a simplified method - in production, you'd want to cache this or get it from JWT
        return pickupPersonService.getAllPickupPersons().stream()
                .filter(person -> person.getEmail().equals(email))
                .findFirst()
                .map(person -> person.getId())
                .orElseThrow(() -> new RuntimeException("Pickup person not found"));
    }

    // Simple DTO for status updates
    public static class StatusUpdateRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    // DTO for OTP verification
    public static class OtpVerificationRequest {
        private String otp;
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
    
    // Test endpoint to verify OTP service
    @PostMapping("/test-otp")
    public ResponseEntity<String> testOtp(@RequestParam String email) {
        try {
            System.out.println("[TEST OTP] Testing OTP generation for email: " + email);
            String otp = pickupPersonService.testOtpGeneration(email);
            return ResponseEntity.ok("Test OTP generated: " + otp + " (check console)");
        } catch (Exception e) {
            System.err.println("[TEST OTP ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Test failed: " + e.getMessage());
        }
    }
    
    // Simple test endpoint without authentication
    @GetMapping("/test-connection")
    public ResponseEntity<String> testConnection() {
        return ResponseEntity.ok("PickupPersonController is working!");
    }
    
    // Health check endpoint to verify all services
    @GetMapping("/health-check")
    public ResponseEntity<String> healthCheck() {
        try {
            StringBuilder status = new StringBuilder();
            status.append("PickupPersonController: OK\n");
            status.append("PickupPersonService: ").append(pickupPersonService != null ? "OK" : "NULL").append("\n");
            
            // Test OTP service
            try {
                String testOtp = pickupPersonService.testOtpGeneration("test@example.com");
                status.append("OtpService: OK (Generated: ").append(testOtp).append(")\n");
            } catch (Exception e) {
                status.append("OtpService: ERROR - ").append(e.getMessage()).append("\n");
            }
            
            return ResponseEntity.ok(status.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Health check failed: " + e.getMessage());
        }
    }
}
