package com.example.SmartScrap.service;

import com.example.SmartScrap.model.EwasteRequest;
import com.example.SmartScrap.model.User;
import com.example.SmartScrap.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private EmailService emailService;

    public void sendRequestCreatedNotifications(EwasteRequest request, User user) {
        // Send notification to user
        sendUserRequestCreatedEmail(user, request);
        
        // Send notification to admin
        sendAdminNewRequestEmail(request, user);
    }

    public void sendStatusUpdateNotification(EwasteRequest request, User user, Status oldStatus, Status newStatus) {
        sendUserStatusUpdateEmail(user, request, oldStatus, newStatus);
    }

    public void sendPickupPersonAssignmentNotification(EwasteRequest request, User pickupPerson) {
        sendPickupPersonAssignmentEmail(request, pickupPerson);
    }

    public void sendCompletionNotifications(EwasteRequest request, User user, User pickupPerson) {
        // Send completion email to user
        sendUserCompletionEmail(user, request, pickupPerson);
        
        // Send completion email to pickup person
        sendPickupPersonCompletionEmail(pickupPerson, request, user);
        
        // Send completion email to admin
        sendAdminCompletionEmail(request, user, pickupPerson);
    }

    private void sendUserRequestCreatedEmail(User user, EwasteRequest request) {
        String subject = "E-Waste Pickup Request Confirmed - SmartScrap";
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c5aa0;">Request Confirmed!</h2>
                    <p>Dear %s,</p>
                    <p>Your e-waste pickup request has been successfully submitted and is now under review.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">Request Details:</h3>
                        <p><strong>Request ID:</strong> #%d</p>
                        <p><strong>Device Type:</strong> %s</p>
                        <p><strong>Brand:</strong> %s</p>
                        <p><strong>Model:</strong> %s</p>
                        <p><strong>Condition:</strong> %s</p>
                        <p><strong>Quantity:</strong> %d</p>
                        <p><strong>Remarks:</strong> %s</p>
                        <p><strong>Pickup Address:</strong> %s</p>
                        <p><strong>Preferred Pickup Date:</strong> %s</p>
                        <p><strong>Status:</strong> <span style="color: #ffc107; font-weight: bold;">%s</span></p>
                    </div>
                    
                    <p>Our team will review your request and contact you within 24-48 hours to schedule the pickup.</p>
                    <p>You can track your request status by logging into your account.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                        <p style="font-size: 14px; color: #666;">
                            Thank you for choosing SmartScrap for your e-waste management needs!<br>
                            <strong>SmartScrap Team</strong>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            user.getFullName(),
            request.getId(),
            request.getDeviceType(),
            request.getBrand(),
            request.getModel(),
            request.getItemCondition(),
            request.getQuantity(),
            request.getRemarks() != null ? request.getRemarks() : "None",
            request.getPickupAddress(),
            request.getPickupDate() != null ? request.getPickupDate().toString() : "Not specified",
            request.getStatus().name()
        );
        
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    private void sendAdminNewRequestEmail(EwasteRequest request, User user) {
        String subject = "New E-Waste Pickup Request - SmartScrap Admin";
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #dc3545;">New Pickup Request Received</h2>
                    <p>A new e-waste pickup request has been submitted and requires your attention.</p>
                    
                    <div style="background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #ffc107;">
                        <h3 style="margin-top: 0; color: #856404;">Request Details:</h3>
                        <p><strong>Request ID:</strong> #%d</p>
                        <p><strong>Customer:</strong> %s (%s)</p>
                        <p><strong>Contact:</strong> %s</p>
                        <p><strong>Device Type:</strong> %s</p>
                        <p><strong>Brand:</strong> %s</p>
                        <p><strong>Model:</strong> %s</p>
                        <p><strong>Condition:</strong> %s</p>
                        <p><strong>Quantity:</strong> %d</p>
                        <p><strong>Remarks:</strong> %s</p>
                        <p><strong>Pickup Address:</strong> %s</p>
                        <p><strong>Preferred Pickup Date:</strong> %s</p>
                        <p><strong>Status:</strong> <span style="color: #ffc107; font-weight: bold;">%s</span></p>
                    </div>
                    
                    <p>Please log into the admin dashboard to review and update the request status.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                        <p style="font-size: 14px; color: #666;">
                            <strong>SmartScrap Admin System</strong>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            request.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getMobileNumber(),
            request.getDeviceType(),
            request.getBrand(),
            request.getModel(),
            request.getItemCondition(),
            request.getQuantity(),
            request.getRemarks() != null ? request.getRemarks() : "None",
            request.getPickupAddress(),
            request.getPickupDate() != null ? request.getPickupDate().toString() : "Not specified",
            request.getStatus().name()
        );
        
        // Send to admin email (you can configure this in application.properties)
        emailService.sendEmail("ramakrishnakattamuri564@gmail.com", subject, body);
    }

    private void sendUserStatusUpdateEmail(User user, EwasteRequest request, Status oldStatus, Status newStatus) {
        String subject = "E-Waste Request Status Update - SmartScrap";
        String statusColor = getStatusColor(newStatus);
        String statusMessage = getStatusMessage(newStatus);
        
        String body = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c5aa0;">Request Status Updated</h2>
                    <p>Dear %s,</p>
                    <p>Your e-waste pickup request status has been updated.</p>
                    
                    <div style="background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #2c5aa0;">Request Details:</h3>
                        <p><strong>Request ID:</strong> #%d</p>
                        <p><strong>Device Type:</strong> %s</p>
                        <p><strong>Previous Status:</strong> <span style="color: #6c757d;">%s</span></p>
                        <p><strong>New Status:</strong> <span style="color: %s; font-weight: bold;">%s</span></p>
                    </div>
                    
                    <div style="background-color: %s; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid %s;">
                        <p style="margin: 0; font-weight: bold;">%s</p>
                    </div>
                    
                    %s
                    
                    <p>You can track your request status by logging into your account.</p>
                    
                    <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;">
                        <p style="font-size: 14px; color: #666;">
                            Thank you for choosing SmartScrap!<br>
                            <strong>SmartScrap Team</strong>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """, 
            user.getFullName(),
            request.getId(),
            request.getDeviceType(),
            oldStatus.name(),
            statusColor,
            newStatus.name(),
            getStatusBackgroundColor(newStatus),
            statusColor,
            statusMessage,
            newStatus == Status.SCHEDULED && request.getPickupDate() != null ? 
                String.format("""
                    <div style="background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0; border-left: 4px solid #2196f3;">
                        <h3 style="margin-top: 0; color: #1976d2;">📅 Scheduled Pickup Details</h3>
                        <p><strong>Pickup Date & Time:</strong> %s</p>
                        <p><strong>Pickup Address:</strong> %s</p>
                        <p style="margin-bottom: 0; font-size: 14px; color: #666;">Please ensure someone is available at the scheduled time.</p>
                    </div>
                    """, 
                    request.getPickupDate().toString(),
                    request.getPickupAddress() != null ? request.getPickupAddress() : "Not specified"
                ) : ""
        );
        
        System.out.println("[EMAIL DEBUG] Sending status update email to: " + user.getEmail());
        System.out.println("[EMAIL DEBUG] New status: " + newStatus);
        System.out.println("[EMAIL DEBUG] Pickup date: " + (request.getPickupDate() != null ? request.getPickupDate().toString() : "Not scheduled"));
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    private String getStatusColor(Status status) {
        return switch (status) {
            case PENDING -> "#ffc107";
            case APPROVED -> "#28a745";
            case SCHEDULED -> "#17a2b8";
            case COMPLETED -> "#28a745";
            case REJECTED -> "#dc3545";
            case CANCELLED -> "#6c757d";
        };
    }

    private String getStatusBackgroundColor(Status status) {
        return switch (status) {
            case PENDING -> "#fff3cd";
            case APPROVED -> "#d4edda";
            case SCHEDULED -> "#d1ecf1";
            case COMPLETED -> "#d4edda";
            case REJECTED -> "#f8d7da";
            case CANCELLED -> "#f8f9fa";
        };
    }

    private String getStatusMessage(Status status) {
        return switch (status) {
            case PENDING -> "Your request is under review. We'll contact you soon!";
            case APPROVED -> "Great news! Your request has been approved. We'll contact you to schedule the pickup.";
            case SCHEDULED -> "Your pickup has been scheduled! Our team will arrive at the specified date and time.";
            case COMPLETED -> "Thank you! Your e-waste has been successfully collected and processed.";
            case REJECTED -> "Unfortunately, your request could not be processed. Please contact us for more information.";
            case CANCELLED -> "Your request has been cancelled. Please contact us if you need assistance.";
        };
    }

    private void sendPickupPersonAssignmentEmail(EwasteRequest request, User pickupPerson) {
        System.out.println("[EMAIL DEBUG] Preparing pickup person assignment email for: " + pickupPerson.getEmail());
        String subject = "New Pickup Assignment - SmartScrap";
        String scheduledDateTimeStr = request.getPickupDate() != null ? request.getPickupDate().toString() : "Not scheduled yet";
        String body = String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #20c997, #17a2b8); color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 8px 8px; }
                    .request-details { background: white; padding: 20px; border-radius: 6px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .detail-row { display: flex; justify-content: space-between; margin: 10px 0; padding: 8px 0; border-bottom: 1px solid #eee; }
                    .detail-label { font-weight: bold; color: #495057; }
                    .detail-value { color: #6c757d; }
                    .highlight { background: #e3f2fd; padding: 15px; border-radius: 6px; margin: 15px 0; border-left: 4px solid #2196f3; }
                    .button { display: inline-block; background: #20c997; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; margin: 10px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🚛 New Pickup Assignment</h1>
                        <p>You have been assigned a new e-waste pickup request</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s!</h2>
                        <p>You have been assigned to handle a new e-waste pickup request. Please review the details below and prepare for the scheduled pickup.</p>
                        
                        <div class="highlight">
                            <h3>📋 Assignment Details</h3>
                            <p><strong>Request ID:</strong> #%d</p>
                            <p><strong>Customer:</strong> %s</p>
                            <p><strong>Contact:</strong> %s</p>
                            <p><strong>Status:</strong> <span style="color: #17a2b8; font-weight: bold;">%s</span></p>
                        </div>
                        
                        <div class="request-details">
                            <h3>📱 Item Details</h3>
                            <div class="detail-row">
                                <span class="detail-label">Device Type:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Brand & Model:</span>
                                <span class="detail-value">%s %s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Condition:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Quantity:</span>
                                <span class="detail-value">%d items</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Remarks:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="request-details">
                            <h3>📍 Pickup Information</h3>
                            <div class="detail-row">
                                <span class="detail-label">Pickup Address:</span>
                                <span class="detail-value">%s</span>
                            </div>
                            <div class="detail-row">
                                <span class="detail-label">Scheduled Date & Time:</span>
                                <span class="detail-value">%s</span>
                            </div>
                        </div>
                        
                        <div class="highlight">
                            <h3>📝 Important Instructions</h3>
                            <ul>
                                <li>Please arrive on time for the scheduled pickup</li>
                                <li>Bring proper identification and company credentials</li>
                                <li>Handle the e-waste items with care during collection</li>
                                <li>Update the request status after successful pickup</li>
                                <li>Contact the customer if you need to reschedule</li>
                            </ul>
                        </div>
                        
                        <p style="font-size: 14px; color: #666;">
                            Thank you for your service!<br>
                            <strong>SmartScrap Management Team</strong>
                        </p>
                    </div>
                </div>
            </body>
            </html>
            """,
            pickupPerson.getFullName(),
            request.getId(),
            request.getUser().getFullName(),
            request.getUser().getEmail(),
            request.getStatus().name(),
            request.getDeviceType(),
            request.getBrand(),
            request.getModel(),
            request.getItemCondition(),
            request.getQuantity(),
            request.getRemarks() != null ? request.getRemarks() : "None",
            request.getPickupAddress(),
            request.getPickupDate() != null ? request.getPickupDate().toString() : "Not scheduled yet"
        );
        
        System.out.println("[EMAIL DEBUG] Sending pickup person assignment email to: " + pickupPerson.getEmail());
        System.out.println("[EMAIL DEBUG] Pickup date: " + (request.getPickupDate() != null ? request.getPickupDate().toString() : "Not scheduled yet"));
        emailService.sendEmail(pickupPerson.getEmail(), subject, body);
        System.out.println("[EMAIL DEBUG] Pickup person assignment email sent");
    }

    private void sendUserCompletionEmail(User user, EwasteRequest request, User pickupPerson) {
        String subject = "E-Waste Pickup Completed Successfully - SmartScrap";
        String body = String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-box { background: #d4edda; border: 1px solid #c3e6cb; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .details { background: white; padding: 20px; border-radius: 6px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Pickup Completed Successfully!</h1>
                        <p>Your e-waste has been collected and processed</p>
                    </div>
                    <div class="content">
                        <h2>Dear %s,</h2>
                        <p>Great news! Your e-waste pickup request has been completed successfully.</p>
                        
                        <div class="success-box">
                            <h3>🎉 Request Completed</h3>
                            <p><strong>Request ID:</strong> #%d</p>
                            <p><strong>Completed by:</strong> %s</p>
                            <p><strong>Completion Date:</strong> %s</p>
                        </div>
                        
                        <div class="details">
                            <h3>📱 Collected Items</h3>
                            <p><strong>Device Type:</strong> %s</p>
                            <p><strong>Brand & Model:</strong> %s %s</p>
                            <p><strong>Quantity:</strong> %d items</p>
                            <p><strong>Condition:</strong> %s</p>
                        </div>
                        
                        <p>Thank you for contributing to environmental sustainability by properly disposing of your e-waste through SmartScrap!</p>
                        
                        <div class="footer">
                            <p>Thank you for choosing SmartScrap!<br><strong>SmartScrap Team</strong></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, 
            user.getFullName(),
            request.getId(),
            pickupPerson.getFullName(),
            java.time.LocalDateTime.now().toString(),
            request.getDeviceType(),
            request.getBrand(),
            request.getModel(),
            request.getQuantity(),
            request.getItemCondition()
        );
        
        emailService.sendEmail(user.getEmail(), subject, body);
    }

    private void sendPickupPersonCompletionEmail(User pickupPerson, EwasteRequest request, User user) {
        String subject = "Pickup Completed Successfully - SmartScrap";
        String body = String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-box { background: #d4edda; border: 1px solid #c3e6cb; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Great Job!</h1>
                        <p>Pickup completed successfully</p>
                    </div>
                    <div class="content">
                        <h2>Hello %s,</h2>
                        <p>You have successfully completed the e-waste pickup assignment.</p>
                        
                        <div class="success-box">
                            <h3>✅ Assignment Completed</h3>
                            <p><strong>Request ID:</strong> #%d</p>
                            <p><strong>Customer:</strong> %s</p>
                            <p><strong>Completion Date:</strong> %s</p>
                        </div>
                        
                        <p>Thank you for your excellent service! The customer has been notified of the successful completion.</p>
                        
                        <div class="footer">
                            <p>Keep up the great work!<br><strong>SmartScrap Management Team</strong></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, 
            pickupPerson.getFullName(),
            request.getId(),
            user.getFullName(),
            java.time.LocalDateTime.now().toString()
        );
        
        emailService.sendEmail(pickupPerson.getEmail(), subject, body);
    }

    private void sendAdminCompletionEmail(EwasteRequest request, User user, User pickupPerson) {
        String subject = "E-Waste Pickup Completed - SmartScrap Admin";
        String body = String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                    .content { background: #f8f9fa; padding: 30px; border-radius: 0 0 10px 10px; }
                    .success-box { background: #d4edda; border: 1px solid #c3e6cb; padding: 20px; border-radius: 8px; margin: 20px 0; }
                    .details { background: white; padding: 20px; border-radius: 6px; margin: 20px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                    .footer { text-align: center; margin-top: 30px; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Pickup Completed</h1>
                        <p>E-waste collection successfully completed</p>
                    </div>
                    <div class="content">
                        <p>An e-waste pickup request has been completed successfully.</p>
                        
                        <div class="success-box">
                            <h3>📋 Completion Summary</h3>
                            <p><strong>Request ID:</strong> #%d</p>
                            <p><strong>Customer:</strong> %s (%s)</p>
                            <p><strong>Pickup Person:</strong> %s (%s)</p>
                            <p><strong>Completion Date:</strong> %s</p>
                        </div>
                        
                        <div class="details">
                            <h3>📱 Collected Items</h3>
                            <p><strong>Device Type:</strong> %s</p>
                            <p><strong>Brand & Model:</strong> %s %s</p>
                            <p><strong>Quantity:</strong> %d items</p>
                            <p><strong>Condition:</strong> %s</p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>SmartScrap Admin System</strong></p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
            """, 
            request.getId(),
            user.getFullName(),
            user.getEmail(),
            pickupPerson.getFullName(),
            pickupPerson.getEmail(),
            java.time.LocalDateTime.now().toString(),
            request.getDeviceType(),
            request.getBrand(),
            request.getModel(),
            request.getQuantity(),
            request.getItemCondition()
        );
        
        emailService.sendEmail("ramakrishnakattamuri564@gmail.com", subject, body);
    }
}