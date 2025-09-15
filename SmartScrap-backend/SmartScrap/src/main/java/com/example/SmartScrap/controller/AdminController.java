package com.example.SmartScrap.controller;

import com.example.SmartScrap.dto.AdminDashboardStatsDto;
import com.example.SmartScrap.dto.AdminRequestViewDto;
import com.example.SmartScrap.dto.AdminStatusUpdateRequest;
import com.example.SmartScrap.dto.PickupPersonRegistrationRequest;
import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.Status;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.EwasteRequestRepository;
import com.example.SmartScrap.repository.UserRepository;
import com.example.SmartScrap.service.NotificationService;
import com.example.SmartScrap.service.PickupPersonService;
import com.example.SmartScrap.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EwasteRequestRepository ewasteRequestRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PickupPersonService pickupPersonService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        // This endpoint can expose passwords, which is a security risk.
        // For a real application, you would create a UserViewDto.
        // For now, this is acceptable for functionality.
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null)); // Avoid sending password hashes
        return ResponseEntity.ok(users);
    }

    // --- MODIFIED: This now uses the safe AdminRequestViewDto ---
    @GetMapping("/requests")
    public ResponseEntity<List<AdminRequestViewDto>> getAllRequests() {
        List<EwasteRequest> allRequests = ewasteRequestRepository.findAll();
        List<AdminRequestViewDto> requestViews = allRequests.stream()
                .map(AdminRequestViewDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requestViews);
    }

    // --- MODIFIED: This is the new, powerful update method ---
    @PutMapping("/requests/{id}/status")
    public ResponseEntity<EwasteRequest> updateRequestStatus(
            @PathVariable Long id,
            @RequestBody AdminStatusUpdateRequest statusUpdate) {

        EwasteRequest request = ewasteRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + id));

        // Store old status for notification
        Status oldStatus = request.getStatus();
        User user = request.getUser();

        // Update the status
        request.setStatus(statusUpdate.getStatus());

        // Handle logic based on the new status
        if (statusUpdate.getStatus() == Status.REJECTED) {
            request.setRejectionReason(statusUpdate.getRejectionReason());
            request.setPickupDate(null); // Clear any previously scheduled date
        } else if (statusUpdate.getStatus() == Status.SCHEDULED) {
            if (statusUpdate.getPickupDate() != null && !statusUpdate.getPickupDate().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime pickupDateTime = LocalDateTime.parse(statusUpdate.getPickupDate(), formatter);
                request.setPickupDate(pickupDateTime);
            }
            request.setRejectionReason(null); // Clear any previous rejection reason
        } else {
            // For any other status, clear these optional fields
            request.setPickupDate(null);
            request.setRejectionReason(null);
        }

        EwasteRequest savedRequest = ewasteRequestRepository.save(request);

        // Send notification to user about status update (only if status actually changed)
        if (oldStatus != statusUpdate.getStatus()) {
            try {
                notificationService.sendStatusUpdateNotification(savedRequest, user, oldStatus, statusUpdate.getStatus());
            } catch (Exception e) {
                System.err.println("Failed to send status update notification: " + e.getMessage());
                // Don't fail the status update if email fails
            }
        }

        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/stats")
    public ResponseEntity<AdminDashboardStatsDto> getStats() {
        long totalUsers = userRepository.count();
        long totalRequests = ewasteRequestRepository.count();
        long pendingRequests = ewasteRequestRepository.findAll().stream().filter(r -> r.getStatus() == Status.PENDING).count();
        long completedRequests = ewasteRequestRepository.findAll().stream().filter(r -> r.getStatus() == Status.COMPLETED).count();

        AdminDashboardStatsDto stats = new AdminDashboardStatsDto(totalUsers, totalRequests, pendingRequests, completedRequests);
        return ResponseEntity.ok(stats);
    }

    // --- NEW: Get request history for a specific user (admin only) ---
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<AdminRequestViewDto>> getUserRequestHistory(@PathVariable Long userId) {
        // Ensure the user exists (optional but useful for clearer errors)
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        List<EwasteRequest> userRequests = ewasteRequestRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        List<AdminRequestViewDto> requestViews = userRequests.stream()
                .map(AdminRequestViewDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(requestViews);
    }

    // --- NEW: Pickup Person Management ---
    @PostMapping("/pickup-persons/register")
    public ResponseEntity<User> registerPickupPerson(@RequestBody PickupPersonRegistrationRequest request) {
        try {
            User pickupPerson = pickupPersonService.registerPickupPerson(request);
            pickupPerson.setPassword(null); // Don't send password back
            return ResponseEntity.ok(pickupPerson);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/pickup-persons")
    public ResponseEntity<List<User>> getAllPickupPersons() {
        List<User> pickupPersons = pickupPersonService.getAllPickupPersons();
        pickupPersons.forEach(person -> person.setPassword(null)); // Don't send passwords
        return ResponseEntity.ok(pickupPersons);
    }

    @PutMapping("/requests/{id}/assign-pickup-person")
    public ResponseEntity<EwasteRequest> assignPickupPerson(
            @PathVariable Long id,
            @RequestParam Long pickupPersonId,
            @RequestParam String scheduledDateTime) {
        try {
            java.time.LocalDateTime dateTime = java.time.LocalDateTime.parse(scheduledDateTime);
            EwasteRequest request = pickupPersonService.assignPickupPerson(id, pickupPersonId, dateTime);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Test endpoint for email functionality
    @PostMapping("/test-email")
    public ResponseEntity<String> testEmail(@RequestParam String to) {
        try {
            String subject = "Test Email - SmartScrap";
            String body = "<h1>Test Email</h1><p>This is a test email from SmartScrap system.</p>";
            emailService.sendEmail(to, subject, body);
            return ResponseEntity.ok("Test email sent to: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send test email: " + e.getMessage());
        }
    }
}