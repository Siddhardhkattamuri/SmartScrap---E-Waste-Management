package com.example.SmartScrap.dto;

import lombok.Data;

@Data
public class SignUpRequest {
    private String fullName;
    private String email;
    private String mobileNumber;
    private String address;
    private String password;
}