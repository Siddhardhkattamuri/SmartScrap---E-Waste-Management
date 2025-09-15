package com.example.SmartScrap.service;

import com.example.SmartScrap.dto.PickupPersonRegistrationRequest;
import com.example.SmartScrap.dto.PickupPersonRequestViewDto;
import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.Role;
import com.example.SmartScrap.model.Status;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.EwasteRequestRepository;
import com.example.SmartScrap.repository.UserRepository;
import com.example.SmartScrap.service.OtpService;
import com.example.SmartScrap.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PickupPersonService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EwasteRequestRepository ewasteRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private OtpService otpService;

    public User registerPickupPerson(PickupPersonRegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        User pickupPerson = new User();
        pickupPerson.setFullName(request.getFullName());
        pickupPerson.setEmail(request.getEmail());
        pickupPerson.setPassword(passwordEncoder.encode(request.getPassword()));
        pickupPerson.setRole(Role.ROLE_PICKUP_PERSON);
        pickupPerson.setMobileNumber(request.getMobileNumber());
        pickupPerson.setAddress(request.getAddress());

        return userRepository.save(pickupPerson);
    }

    public List<PickupPersonRequestViewDto> getPickupPersonRequests(Long pickupPersonId) {
        List<EwasteRequest> requests = ewasteRequestRepository.findByPickupPerson_IdOrderByPickupDateAsc(pickupPersonId);
        return requests.stream()
                .map(PickupPersonRequestViewDto::new)
                .collect(Collectors.toList());
    }

    public List<User> getAllPickupPersons() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.ROLE_PICKUP_PERSON)
                .collect(Collectors.toList());
    }

    public EwasteRequest assignPickupPerson(Long requestId, Long pickupPersonId, java.time.LocalDateTime scheduledDateTime) {
        EwasteRequest request = ewasteRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

        User pickupPerson = userRepository.findById(pickupPersonId)
                .orElseThrow(() -> new RuntimeException("Pickup person not found with id: " + pickupPersonId));

        if (pickupPerson.getRole() != Role.ROLE_PICKUP_PERSON) {
            throw new RuntimeException("User is not a pickup person");
        }

        request.setPickupPerson(pickupPerson);
        request.setPickupDate(scheduledDateTime);
        request.setStatus(Status.SCHEDULED);
        EwasteRequest savedRequest = ewasteRequestRepository.save(request);

        // Send notification to pickup person with scheduled date/time
        try {
            System.out.println("[PICKUP ASSIGNMENT] Sending notification to pickup person: " + pickupPerson.getEmail());
            notificationService.sendPickupPersonAssignmentNotification(savedRequest, pickupPerson);
            System.out.println("[PICKUP ASSIGNMENT] Notification sent successfully");
        } catch (Exception e) {
            System.err.println("[PICKUP ASSIGNMENT ERROR] Failed to send pickup person assignment notification: " + e.getMessage());
            e.printStackTrace();
            // Don't fail the assignment if email fails
        }

        return savedRequest;
    }

    public EwasteRequest updateRequestStatus(Long requestId, Long pickupPersonId, String status) {
        EwasteRequest request = ewasteRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

        // Verify that this pickup person is assigned to this request
        if (request.getPickupPerson() == null || !request.getPickupPerson().getId().equals(pickupPersonId)) {
            throw new RuntimeException("You are not assigned to this request");
        }

        // Update the status
        request.setStatus(Status.valueOf(status));
        return ewasteRequestRepository.save(request);
    }

    // Optimized method to initiate completion with OTP
    public String initiateCompletion(Long requestId, Long pickupPersonId) {
        System.out.println("[INITIATE COMPLETION] Starting completion for request: " + requestId + ", pickup person: " + pickupPersonId);
        
        try {
            // Check if services are available
            if (otpService == null) {
                System.err.println("[INITIATE COMPLETION] ERROR: OtpService is null!");
                throw new RuntimeException("OtpService is not available");
            }
            
            if (notificationService == null) {
                System.err.println("[INITIATE COMPLETION] ERROR: NotificationService is null!");
                throw new RuntimeException("NotificationService is not available");
            }
            
            EwasteRequest request = ewasteRequestRepository.findById(requestId)
                    .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

            System.out.println("[INITIATE COMPLETION] Request found: " + request.getId() + ", Status: " + request.getStatus());
            System.out.println("[INITIATE COMPLETION] Assigned pickup person: " + (request.getPickupPerson() != null ? request.getPickupPerson().getId() : "None"));

            // Verify that this pickup person is assigned to this request
            if (request.getPickupPerson() == null || !request.getPickupPerson().getId().equals(pickupPersonId)) {
                System.out.println("[INITIATE COMPLETION] ERROR: Pickup person not assigned to this request");
                throw new RuntimeException("You are not assigned to this request");
            }

            // Check if request is in SCHEDULED status
            if (request.getStatus() != Status.SCHEDULED) {
                System.out.println("[INITIATE COMPLETION] ERROR: Request not in SCHEDULED status. Current status: " + request.getStatus());
                throw new RuntimeException("Request must be in SCHEDULED status to complete");
            }

            // Generate and send OTP to customer
            String customerEmail = request.getUser().getEmail();
            String customerName = request.getUser().getFullName();
            String requestIdStr = request.getId().toString();
            
            System.out.println("[INITIATE COMPLETION] Customer email: " + customerEmail + ", Name: " + customerName);
            
            // Create OTP and send email
            System.out.println("[INITIATE COMPLETION] Creating OTP...");
            String otp = otpService.createOtp(customerEmail, requestId);
            System.out.println("[INITIATE COMPLETION] OTP created: " + otp);
            
            // Send OTP email
            System.out.println("[INITIATE COMPLETION] Sending OTP email...");
            otpService.sendOtpEmail(customerEmail, otp, customerName, requestIdStr);
            System.out.println("[INITIATE COMPLETION] OTP email sent successfully");
            
            System.out.println("[INITIATE COMPLETION] Returning success message");
            return "OTP sent to customer successfully";
            
        } catch (Exception e) {
            System.err.println("[INITIATE COMPLETION] ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate completion: " + e.getMessage());
        }
    }

    // Optimized method to verify OTP and complete request
    public EwasteRequest verifyOtpAndComplete(Long requestId, Long pickupPersonId, String otp) {
        EwasteRequest request = ewasteRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found with id: " + requestId));

        // Verify that this pickup person is assigned to this request
        if (request.getPickupPerson() == null || !request.getPickupPerson().getId().equals(pickupPersonId)) {
            throw new RuntimeException("You are not assigned to this request");
        }

        // Verify OTP
        String customerEmail = request.getUser().getEmail();
        if (!otpService.verifyOtp(customerEmail, otp, requestId)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        // Update status to completed
        request.setStatus(Status.COMPLETED);
        EwasteRequest savedRequest = ewasteRequestRepository.save(request);

        // Send completion notifications in background for better performance
        new Thread(() -> {
            try {
                notificationService.sendCompletionNotifications(savedRequest, request.getUser(), request.getPickupPerson());
            } catch (Exception e) {
                System.err.println("[COMPLETION EMAIL ERROR] Failed to send completion emails: " + e.getMessage());
            }
        }).start();

        return savedRequest;
    }
    
    // Test method to verify OTP generation
    public String testOtpGeneration(String email) {
        try {
            System.out.println("[TEST OTP] Starting OTP test for email: " + email);
            String otp = otpService.createOtp(email, 999L); // Use 999 as test request ID
            System.out.println("[TEST OTP] OTP created: " + otp);
            
            // Test email sending
            otpService.sendOtpEmail(email, otp, "Test User", "999");
            System.out.println("[TEST OTP] OTP email sent successfully");
            
            return otp;
        } catch (Exception e) {
            System.err.println("[TEST OTP ERROR] " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
