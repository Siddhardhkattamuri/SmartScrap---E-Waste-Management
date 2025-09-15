package com.example.SmartScrap.controller;

import com.example.SmartScrap.dto.*;
import com.example.SmartScrap.model.Role;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.UserRepository;
import com.example.SmartScrap.security.jwt.JwtUtils;
import com.example.SmartScrap.service.EmailService;
import com.example.SmartScrap.service.OtpService;
import com.example.SmartScrap.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Keep for development
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    // ... (All Autowired fields are the same)

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    OtpService otpService;

    @Autowired
    EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        // ... (This method is unchanged)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                roles));
    }

    // --- OTP Login Flow ---
    @PostMapping("/login-otp/init")
    public ResponseEntity<?> initOtp(@RequestBody LoginRequest loginRequest) {
        try {
            // First verify username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            // If authentication successful, get user details
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getEmail()).orElse(null);
            
            if (user == null) {
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }
            
            // Generate and send OTP
            String code = otpService.createOtp(user.getEmail(), 0L);
            emailService.sendOtp(user.getEmail(), code);
            
            return ResponseEntity.ok(new MessageResponse("Credentials verified. OTP sent to email"));
            
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid email or password"));
        }
    }

    @PostMapping("/login-otp/verify")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerifyRequest req) {
        try {
            System.out.println("OTP Verification attempt for email: " + req.getEmail() + ", OTP: " + req.getOtp());
            
            // Validate input
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                System.out.println("Email is null or empty");
                return ResponseEntity.badRequest().body(new MessageResponse("Email is required"));
            }
            
            if (req.getOtp() == null || req.getOtp().trim().isEmpty()) {
                System.out.println("OTP is null or empty");
                return ResponseEntity.badRequest().body(new MessageResponse("OTP is required"));
            }
            
            // Debug: Show all stored OTPs before verification
            otpService.debugStoredOtps();
            
            boolean ok = otpService.verifyOtp(req.getEmail(), req.getOtp(), 0L);
            System.out.println("OTP verification result: " + ok);
            
            if (!ok) {
                System.out.println("OTP verification failed - invalid or expired");
                return ResponseEntity.badRequest().body(new MessageResponse("Invalid or expired OTP"));
            }

            // On success, issue JWT
            User user = userRepository.findByEmail(req.getEmail()).orElse(null);
            if (user == null) {
                System.out.println("User not found for email: " + req.getEmail());
                return ResponseEntity.badRequest().body(new MessageResponse("User not found"));
            }

            System.out.println("User found: " + user.getEmail() + ", Role: " + user.getRole());

            // Build auth principal manually
            UserDetailsImpl userDetails = UserDetailsImpl.build(user);
            System.out.println("UserDetails built successfully");
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            System.out.println("Authentication object created");
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println("Authentication set in security context");
            
            String jwt = jwtUtils.generateJwtToken(authentication);
            System.out.println("JWT generated: " + (jwt != null ? "SUCCESS" : "FAILED"));

            java.util.List<String> roles = java.util.Collections.singletonList(user.getRole().name());
            System.out.println("JWT generated successfully, roles: " + roles);
            
            JwtResponse response = new JwtResponse(jwt, user.getId(), user.getEmail(), roles);
            System.out.println("JwtResponse created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("ERROR in OTP verification: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(new MessageResponse("Internal server error: " + e.getMessage()));
        }
    }
    
    // Debug endpoint to check stored OTPs
    @GetMapping("/debug/otps")
    public ResponseEntity<?> debugOtps() {
        otpService.debugStoredOtps();
        return ResponseEntity.ok("Check console for OTP debug info");
    }
    
    // Test endpoint to generate and verify OTP
    @PostMapping("/test-otp")
    public ResponseEntity<?> testOtp(@RequestParam String email) {
        String code = otpService.createOtp(email, 999L); // Use 999 as test request ID
        System.out.println("=".repeat(60));
        System.out.println("🧪 TEST OTP GENERATED AND STORED:");
        System.out.println("📧 Email: " + email);
        System.out.println("🔢 OTP: " + code);
        System.out.println("=".repeat(60));
        return ResponseEntity.ok("Test OTP generated: " + code + " (check console)");
    }
    
    // Test endpoint to verify OTP
    @PostMapping("/test-verify-otp")
    public ResponseEntity<?> testVerifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean isValid = otpService.verifyOtp(email, otp, 999L);
        return ResponseEntity.ok("OTP verification result: " + isValid);
    }
    
    @PostMapping("/test-pickup-otp")
    public ResponseEntity<?> testPickupOtp(@RequestParam String email, @RequestParam Long requestId) {
        try {
            System.out.println("[TEST PICKUP OTP] Testing pickup OTP for email: " + email + ", requestId: " + requestId);
            String otp = otpService.createOtp(email, requestId);
            System.out.println("[TEST PICKUP OTP] OTP created: " + otp);
            
            // Test the sendOtpEmail method
            otpService.sendOtpEmail(email, otp, "Test Customer", requestId.toString());
            System.out.println("[TEST PICKUP OTP] OTP email sent successfully");
            
            return ResponseEntity.ok("Pickup OTP test successful: " + otp + " (check console)");
        } catch (Exception e) {
            System.err.println("[TEST PICKUP OTP ERROR] " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Pickup OTP test failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        User user = new User();
        user.setFullName(signUpRequest.getFullName());
        user.setEmail(signUpRequest.getEmail());
        user.setMobileNumber(signUpRequest.getMobileNumber());
        user.setAddress(signUpRequest.getAddress());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setRole(Role.ROLE_USER);

        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}