package com.example.SmartScrap.dto;

import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.Status;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class PickupPersonRequestViewDto {
    private Long id;
    private String customerName;
    private String customerEmail;
    private String customerMobile;
    private String deviceType;
    private String brand;
    private String model;
    private String itemCondition;
    private int quantity;
    private String imagePaths;
    private String remarks;
    private String pickupAddress;
    private String pickupDate;
    private String createdAt;
    private Status status;

    public PickupPersonRequestViewDto(EwasteRequest request) {
        this.id = request.getId();
        this.customerName = request.getUser().getFullName();
        this.customerEmail = request.getUser().getEmail();
        this.customerMobile = request.getUser().getMobileNumber();
        this.deviceType = request.getDeviceType();
        this.brand = request.getBrand();
        this.model = request.getModel();
        this.itemCondition = request.getItemCondition();
        this.quantity = request.getQuantity();
        this.imagePaths = request.getImagePaths();
        this.remarks = request.getRemarks();
        this.pickupAddress = request.getPickupAddress();
        
        if (request.getPickupDate() != null) {
            this.pickupDate = request.getPickupDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        if (request.getCreatedAt() != null) {
            this.createdAt = request.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        this.status = request.getStatus();
    }
}
