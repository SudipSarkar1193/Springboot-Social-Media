package com.SSarkar.Xplore.service.contract;

import jakarta.mail.MessagingException;

public interface EmailService {
    public void sendOtpEmail(String to, String otp) throws MessagingException;
}
