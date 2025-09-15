package com.example.SmartScrap.controller;

import com.example.SmartScrap.dto.UserProfileDto;
import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.EwasteRequestRepository;
import com.example.SmartScrap.repository.UserRepository;
import com.example.SmartScrap.service.EwasteRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Use this for modern security
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private EwasteRequestRepository ewasteRequestRepository;
    @Autowired private EwasteRequestService ewasteRequestService;

    // A helper method to get the User entity from the security context
    private User getUserFromUserDetails(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + userDetails.getUsername()));
    }

    @PostMapping("/requests")
    public ResponseEntity<EwasteRequest> createRequest(
            @AuthenticationPrincipal UserDetails userDetails, // This is the modern, reliable way to get the user
            @RequestParam("deviceType") String deviceType,
            @RequestParam("brand") String brand,
            @RequestParam("model") String model,
            @RequestParam("condition") String condition,
            @RequestParam("quantity") int quantity,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("remarks") String remarks,
            @RequestParam(value = "pickupDate", required = false) String pickupDate,
            @RequestParam(value = "files", required = false) MultipartFile[] files) {
        
        User currentUser = getUserFromUserDetails(userDetails);
        EwasteRequest savedRequest = ewasteRequestService.createNewRequest(currentUser, deviceType, brand, model, condition, quantity, pickupAddress, remarks, pickupDate, files);
        return ResponseEntity.ok(savedRequest);
    }

    @GetMapping("/requests")
    public ResponseEntity<List<EwasteRequest>> getMyRequests(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);
        List<EwasteRequest> requests = ewasteRequestRepository.findByUserOrderByCreatedAtDesc(currentUser);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getMyProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = getUserFromUserDetails(userDetails);
        UserProfileDto profile = new UserProfileDto();
        profile.setFullName(currentUser.getFullName());
        profile.setEmail(currentUser.getEmail());
        profile.setMobileNumber(currentUser.getMobileNumber());
        profile.setAddress(currentUser.getAddress());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateMyProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserProfileDto profileDto) {
        User currentUser = getUserFromUserDetails(userDetails);
        currentUser.setFullName(profileDto.getFullName());
        currentUser.setMobileNumber(profileDto.getMobileNumber());
        currentUser.setAddress(profileDto.getAddress());
        userRepository.save(currentUser);
        return ResponseEntity.ok(profileDto);
    }
}