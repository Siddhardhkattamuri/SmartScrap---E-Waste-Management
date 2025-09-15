package com.example.SmartScrap.service;

import com.example.SmartScrap.model.OtpVerification;
import com.example.SmartScrap.repository.OtpVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpVerificationRepository otpRepository;

    @Autowired
    private EmailService emailService;

    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        String otpString = String.valueOf(otp);
        System.out.println("🔐 GENERATED OTP: " + otpString);
        return otpString;
    }

    public String createOtp(String email, Long requestId) {
        try {
            System.out.println("[OTP CREATE] Starting OTP creation for email: " + email + ", requestId: " + requestId);
            
            // Check if repository is available
            if (otpRepository == null) {
                System.err.println("[OTP CREATE] ERROR: OtpVerificationRepository is null!");
                throw new RuntimeException("OtpVerificationRepository is not available");
            }
            
            // Invalidate any existing OTP for this email and request
            Optional<OtpVerification> existingOtp = otpRepository.findByEmailAndRequestIdAndIsUsedFalse(email, requestId);
            if (existingOtp.isPresent()) {
                System.out.println("[OTP CREATE] Invalidating existing OTP");
                existingOtp.get().setUsed(true);
                otpRepository.save(existingOtp.get());
            }

            String otp = generateOtp();
            System.out.println("[OTP CREATE] Generated OTP: " + otp);
            
            OtpVerification otpVerification = new OtpVerification(otp, email, requestId);
            System.out.println("[OTP CREATE] Created OtpVerification object");
            
            otpRepository.save(otpVerification);
            System.out.println("[OTP CREATE] Saved OTP to database");
            
            System.out.println("=".repeat(50));
            System.out.println("🔐 OTP CREATED AND STORED:");
            System.out.println("📧 Email: " + email);
            System.out.println("🔢 OTP: " + otp);
            System.out.println("📋 Request ID: " + requestId);
            System.out.println("=".repeat(50));
            return otp;
        } catch (Exception e) {
            System.err.println("[OTP CREATE] ERROR: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create OTP: " + e.getMessage());
        }
    }

    public boolean verifyOtp(String email, String otp, Long requestId) {
        System.out.println("[OTP VERIFY] Attempting to verify OTP for email: " + email + ", OTP: " + otp + ", RequestID: " + requestId);
        
        // Use the simpler method to find OTP by email, requestId, and OTP code
        Optional<OtpVerification> otpEntity = otpRepository.findByEmailAndOtpAndRequestIdAndIsUsedFalse(email, otp, requestId);
        
        if (otpEntity.isPresent()) {
            OtpVerification otpVerification = otpEntity.get();
            System.out.println("[OTP VERIFY] Found OTP in DB: " + otpVerification.getOtp() + ", Used: " + otpVerification.isUsed() + ", Expires: " + otpVerification.getExpiresAt());
            
            // Check if OTP is expired
            if (otpVerification.getExpiresAt().isBefore(LocalDateTime.now())) {
                System.out.println("[OTP VERIFY] OTP has EXPIRED - expires at: " + otpVerification.getExpiresAt() + ", current time: " + LocalDateTime.now());
                return false;
            }
            
            // Mark OTP as used
            otpVerification.setUsed(true);
            otpRepository.save(otpVerification);
            System.out.println("[OTP VERIFY] OTP verification SUCCESS");
            return true;
        } else {
            System.out.println("[OTP VERIFY] No OTP found for email: " + email + ", OTP: " + otp + ", requestId: " + requestId);
            
            // Debug: Show all OTPs for this email and requestId
            List<OtpVerification> allOtps = otpRepository.findAll();
            System.out.println("[OTP VERIFY DEBUG] All OTPs in database:");
            for (OtpVerification o : allOtps) {
                System.out.println("  - Email: " + o.getEmail() + ", OTP: " + o.getOtp() + ", RequestID: " + o.getRequestId() + ", Used: " + o.isUsed());
            }
        }
        
        System.out.println("[OTP VERIFY] OTP verification FAILED");
        return false;
    }
        
    public void sendOtpEmail(String email, String otp, String customerName, String requestId) {
        System.out.println("[OTP EMAIL] Sending OTP email to: " + email + ", OTP: " + otp + ", Customer: " + customerName + ", RequestID: " + requestId);
        String subject = "OTP Verification for E-Waste Pickup Completion";
        
        String htmlContent = String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>OTP Verification</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; }
                    .otp-box { background: #fff; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 8px; }
                    .otp-code { font-size: 32px; font-weight: bold; color: #667eea; letter-spacing: 5px; }
                    .warning { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 OTP Verification Required</h1>
                        <p>E-Waste Pickup Completion</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>Your pickup person is ready to complete the e-waste collection for <strong>Request #%s</strong>.</p>
                        <p>To ensure the pickup is legitimate and completed successfully, please provide the following OTP to your pickup person:</p>
                        
                        <div class="otp-box">
                            <p style="margin: 0 0 10px 0; color: #666;">Your verification code is:</p>
                            <div class="otp-code">%s</div>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul style="margin: 10px 0 0 0; padding-left: 20px;">
                                <li>This OTP is valid for <strong>10 minutes</strong> only</li>
                                <li>Do not share this OTP with anyone except your verified pickup person</li>
                                <li>If you didn't request this pickup, please contact us immediately</li>
                            </ul>
                        </div>
                        
                        <p>Once the pickup person enters this OTP, your request will be marked as completed and you'll receive a confirmation email.</p>
                    </div>
                    <div class="footer">
                        <p>Thank you for using SmartScrap E-Waste Management System</p>
                        <p>This is an automated message, please do not reply to this email.</p>
                    </div>
                </div>
            </body>
            </html>
            """, customerName, requestId, otp);

        try {
            emailService.sendEmail(email, subject, htmlContent);
            System.out.println("[OTP EMAIL] OTP sent successfully to: " + email);
        } catch (Exception e) {
            System.err.println("[OTP EMAIL ERROR] Failed to send OTP email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanupExpiredOtps() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(1);
        otpRepository.deleteByCreatedAtBefore(cutoffTime);
    }

    public void debugStoredOtps() {
        System.out.println("=== DEBUG: All Stored OTPs ===");
        List<OtpVerification> allOtps = otpRepository.findAll();
        for (OtpVerification otp : allOtps) {
            System.out.println("ID: " + otp.getId() + 
                             ", Email: " + otp.getEmail() + 
                             ", OTP: " + otp.getOtp() + 
                             ", RequestID: " + otp.getRequestId() + 
                             ", Used: " + otp.isUsed() + 
                             ", Created: " + otp.getCreatedAt() + 
                             ", Expires: " + otp.getExpiresAt());
        }
        System.out.println("=== END DEBUG ===");
    }
}