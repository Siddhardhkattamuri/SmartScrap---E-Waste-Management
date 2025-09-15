package com.example.SmartScrap.dto;

import com.example.SmartScrap.model.Status;
import lombok.Data;

@Data
public class AdminStatusUpdateRequest {
    private Status status;
    private String pickupDate; // We'll receive this as a String from the frontend
    private String rejectionReason;
}