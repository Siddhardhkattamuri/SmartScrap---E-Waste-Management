package com.example.SmartScrap.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "ewaste_requests")
@Data
public class EwasteRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_person_id")
    private User pickupPerson;

    private String deviceType;
    private String brand;
    private String model;
    private String itemCondition; // Correct name to avoid SQL keyword conflict
    private int quantity;
    private String imagePaths;
    private String remarks;
    private String rejectionReason;
    private String pickupAddress;
    private LocalDateTime pickupDate; // For when a pickup is scheduled
    @Column(nullable = false)
    private LocalDateTime createdAt; // The field that MUST exist in the database

    @Enumerated(EnumType.STRING)
    private Status status;
}