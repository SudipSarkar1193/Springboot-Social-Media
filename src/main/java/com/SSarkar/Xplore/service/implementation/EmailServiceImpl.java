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
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f4f4;
                        margin: 0;
                        padding: 0;
                    }
                    .container {
                        max-width: 600px;
                        margin: 30px auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        overflow: hidden;
                        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
                    }
                    .header {
                        background-color: #4CAF50;
                        padding: 20px;
                        text-align: center;
                        color: #ffffff;
                        font-size: 20px;
                        font-weight: bold;
                    }
                    .content {
                        padding: 30px;
                        color: #333333;
                        line-height: 1.6;
                    }
                    .otp-box {
                        background-color: #f1f1f1;
                        border: 1px solid #dddddd;
                        padding: 15px;
                        text-align: center;
                        font-size: 24px;
                        font-weight: bold;
                        letter-spacing: 3px;
                        margin: 20px 0;
                        border-radius: 4px;
                    }
                    .footer {
                        background-color: #f9f9f9;
                        padding: 15px;
                        text-align: center;
                        font-size: 12px;
                        color: #777777;
                        border-top: 1px solid #eeeeee;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">Xplore Verification</div>
                    <div class="content">
                        <p>Hi,</p>
                        <p>Use the following verification code to complete your registration on <strong>Xplore</strong>:</p>
                        <div class="otp-box">%s</div>
                        <p>This code will expire in <strong>5 minutes</strong> for security purposes.</p>
                        <p>If you did not request this code, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        &copy; %d Xplore. All rights reserved.
                    </div>
                </div>
            </body>
            </html>
            """, otp, java.time.Year.now().getValue());
    }
}