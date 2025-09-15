package com.example.SmartScrap.service;

import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.Status;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.EwasteRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EwasteRequestService {

    @Autowired
    private EwasteRequestRepository ewasteRequestRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public EwasteRequest createNewRequest(User currentUser, String deviceType, String brand, String model, String condition, int quantity, String pickupAddress, String remarks, String pickupDate, MultipartFile[] files) {
        
        List<String> imagePaths = new ArrayList<>();
        if (files != null && files.length > 0) {
            imagePaths = Arrays.stream(files)
                               .map(fileStorageService::storeFile)
                               .collect(Collectors.toList());
        }

        EwasteRequest newRequest = new EwasteRequest();
        newRequest.setDeviceType(deviceType);
        newRequest.setBrand(brand);
        newRequest.setModel(model);
        newRequest.setItemCondition(condition);
        newRequest.setQuantity(quantity);
        newRequest.setPickupAddress(pickupAddress.trim().isEmpty() ? currentUser.getAddress() : pickupAddress);
        if (pickupDate != null && !pickupDate.isEmpty()) {
            try {
                newRequest.setPickupDate(java.time.LocalDateTime.parse(pickupDate));
            } catch (Exception ignored) {
                // If parsing fails, leave as null
            }
        }
        newRequest.setRemarks(remarks);
        newRequest.setImagePaths(String.join(",", imagePaths)); // Store as comma-separated string
        newRequest.setCreatedAt(LocalDateTime.now());
        newRequest.setStatus(Status.PENDING);
        
        // Synchronize the bidirectional relationship
        newRequest.setUser(currentUser);
        currentUser.getRequests().add(newRequest);

        EwasteRequest savedRequest = ewasteRequestRepository.save(newRequest);
        
        // Send notifications
        try {
            notificationService.sendRequestCreatedNotifications(savedRequest, currentUser);
        } catch (Exception e) {
            System.err.println("Failed to send notifications: " + e.getMessage());
            // Don't fail the request creation if email fails
        }

        return savedRequest;
    }
}