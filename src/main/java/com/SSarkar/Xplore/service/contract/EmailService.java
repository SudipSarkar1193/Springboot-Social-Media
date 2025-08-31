package com.SSarkar.Xplore.service.contract;

import jakarta.mail.MessagingException;

public interface EmailService {
    void sendOtpEmail(String to, String otp) throws MessagingException;
    void sendNotificationEmail(String to, String subject, String message, String actorName, String actorProfilePicUrl, String postContent, String postUrl ) throws MessagingException;
}