package com.example.SmartScrap.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    public void sendOtp(String to, String code) {
        try {
            if (mailSender == null || from == null || from.isBlank()) {
                System.out.println("[DEV EMAIL] To: " + to + " | OTP: " + code);
                return;
            }
            
            String subject = "Your SmartScrap Login OTP";
            String htmlBody = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f8f9fa;">
                        <div style="background-color: white; border-radius: 10px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                            <div style="text-align: center; margin-bottom: 30px;">
                                <h1 style="color: #2c5aa0; margin: 0; font-size: 28px;">SmartScrap</h1>
                                <p style="color: #666; margin: 5px 0 0 0; font-size: 16px;">E-Waste Management System</p>
                            </div>
                            
                            <h2 style="color: #2c5aa0; text-align: center; margin-bottom: 20px;">Login Verification Code</h2>
                            
                            <p style="font-size: 16px; margin-bottom: 20px;">Hello,</p>
                            <p style="font-size: 16px; margin-bottom: 25px;">You have requested to log in to your SmartScrap account. Please use the following verification code to complete your login:</p>
                            
                            <div style="text-align: center; margin: 30px 0;">
                                <div style="display: inline-block; background: linear-gradient(135deg, #2c5aa0, #1e3a8a); color: white; padding: 20px 40px; border-radius: 10px; font-size: 32px; font-weight: bold; letter-spacing: 5px; box-shadow: 0 4px 15px rgba(44, 90, 160, 0.3);">
                                    %s
                                </div>
                            </div>
                            
                            <div style="background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 8px; padding: 15px; margin: 25px 0;">
                                <p style="margin: 0; color: #856404; font-size: 14px; text-align: center;">
                                    <strong>⏰ This code will expire in 5 minutes</strong>
                                </p>
                            </div>
                            
                            <div style="background-color: #f8f9fa; border-radius: 8px; padding: 20px; margin: 25px 0;">
                                <h3 style="color: #2c5aa0; margin-top: 0; font-size: 18px;">Security Tips:</h3>
                                <ul style="color: #666; font-size: 14px; margin: 0; padding-left: 20px;">
                                    <li>Never share this code with anyone</li>
                                    <li>SmartScrap will never ask for your OTP via phone or email</li>
                                    <li>If you didn't request this code, please ignore this email</li>
                                </ul>
                            </div>
                            
                            <p style="font-size: 14px; color: #666; text-align: center; margin: 30px 0 0 0;">
                                If you're having trouble logging in, please contact our support team.
                            </p>
                            
                            <div style="border-top: 1px solid #eee; margin-top: 30px; padding-top: 20px; text-align: center;">
                                <p style="font-size: 12px; color: #999; margin: 0;">
                                    This is an automated message. Please do not reply to this email.<br>
                                    <strong>SmartScrap Team</strong>
                                </p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
                """, code);
            
            sendEmail(to, subject, htmlBody);
            
        } catch (Exception e) {
            System.out.println("[EMAIL ERROR] " + e.getMessage());
        }
    }

    public void sendEmail(String to, String subject, String htmlBody) {
        try {
            System.out.println("[EMAIL SERVICE] Attempting to send email to: " + to);
            System.out.println("[EMAIL SERVICE] Mail sender available: " + (mailSender != null));
            System.out.println("[EMAIL SERVICE] From address: " + from);
            
            if (mailSender == null || from == null || from.isBlank()) {
                System.out.println("[DEV EMAIL] To: " + to + " | Subject: " + subject);
                System.out.println("[DEV EMAIL] Body: " + htmlBody);
                return;
            }
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setFrom(from);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true indicates HTML content
            
            System.out.println("[EMAIL SERVICE] Sending email...");
            mailSender.send(message);
            System.out.println("[EMAIL SENT] To: " + to + " | Subject: " + subject);
        } catch (Exception e) {
            System.out.println("[EMAIL ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }
}




