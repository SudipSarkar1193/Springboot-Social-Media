package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.service.contract.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementation of the EmailService interface for sending OTP emails.
 * Uses JavaMailSender for email delivery.
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String OTP_EMAIL_SUBJECT = "Your OTP for Xplore Registration";

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(OTP_EMAIL_SUBJECT);
        helper.setText(buildOtpEmailContent(otp), true);

        javaMailSender.send(message);
        System.out.println("Email sent to " + to + " with OTP: " + otp);
    }

    private String buildOtpEmailContent(String otp) {
        return String.format(
                "<html>" +
                        "<body style=\"font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;\">" +
                        "<h2 style=\"color: #333; margin-bottom: 20px;\">Verification Code</h2>" +
                        "<p style=\"margin-bottom: 15px;\">Please use the following verification code to complete your registration:</p>" +
                        "<div style=\"background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 4px; padding: 15px; text-align: center; margin: 20px 0;\">" +
                        "<h3 style=\"color: #333; margin: 0; font-size: 24px; letter-spacing: 3px;\">%s</h3>" +
                        "</div>" +
                        "<p style=\"margin-bottom: 15px;\">This code will expire in 5 minutes for security purposes.</p>" +
                        "<hr style=\"border: none; border-top: 1px solid #e9ecef; margin: 20px 0;\">" +
                        "<p style=\"font-size: 14px; color: #666; margin: 0;\">" +
                        "If you did not request this verification code, please ignore this email." +
                        "</p>" +
                        "</body>" +
                        "</html>",
                otp
        );
    }
}