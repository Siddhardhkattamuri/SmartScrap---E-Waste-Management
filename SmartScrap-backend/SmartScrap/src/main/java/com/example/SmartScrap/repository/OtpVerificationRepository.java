package com.example.SmartScrap.repository;

import com.example.SmartScrap.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    
    Optional<OtpVerification> findByEmailAndRequestIdAndIsUsedFalse(String email, Long requestId);
    
    Optional<OtpVerification> findByEmailAndOtpAndRequestIdAndIsUsedFalse(String email, String otp, Long requestId);
    
    Optional<OtpVerification> findTopByEmailAndRequestIdAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(String email, Long requestId, LocalDateTime now);
    
    List<OtpVerification> findByCreatedAtBefore(LocalDateTime dateTime);
    
    void deleteByCreatedAtBefore(LocalDateTime dateTime);
}

