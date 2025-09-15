package com.example.SmartScrap.service;

// Update these imports to match your actual package structure
import com.example.SmartScrap.model.Role;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${smartscrap.app.admin.email}")
    private String adminEmail;

    @Value("${smartscrap.app.admin.password}")
    private String adminPassword;
    
    @Value("${smartscrap.app.admin.fullName}")
    private String adminFullName;

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setFullName(adminFullName);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ROLE_ADMIN);
            admin.setAddress("Admin Headquarters");
            admin.setMobileNumber("0000000000");

            userRepository.save(admin);
            System.out.println(">>> Created default admin user <<<");
        }
    }
}