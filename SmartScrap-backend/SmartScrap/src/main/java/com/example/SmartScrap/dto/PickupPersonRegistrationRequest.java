package com.example.SmartScrap.dto;

import lombok.Data;

@Data
public class PickupPersonRegistrationRequest {
    private String fullName;
    private String email;
    private String password;
    private String mobileNumber;
    private String address;
}
