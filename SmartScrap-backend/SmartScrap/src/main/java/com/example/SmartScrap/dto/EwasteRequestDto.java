package com.example.SmartScrap.dto;

import lombok.Data;

@Data
public class EwasteRequestDto {
    private String itemDescription;
    private int quantity;
    private String pickupAddress;
}