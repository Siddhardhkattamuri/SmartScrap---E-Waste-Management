package com.example.SmartScrap.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    // ... (id, fullName, email, password, role are the same)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    
    // --- NEW FIELDS ---
    private String mobileNumber;
    private String address;


    @JsonManagedReference
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EwasteRequest> requests = new ArrayList<>();

    @JsonManagedReference
    @OneToMany(mappedBy = "pickupPerson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EwasteRequest> pickupRequests = new ArrayList<>();
}