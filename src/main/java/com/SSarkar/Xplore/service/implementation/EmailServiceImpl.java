package com.SSarkar.Xplore.service.implementation;

import com.SSarkar.Xplore.service.contract.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of the EmailService interface for sending emails.
 * Uses JavaMailSender for email delivery with enhanced error handling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String OTP_EMAIL_SUBJECT = "Your OTP for Xplore Registration";
    private static final int MAX_POST_CONTENT_LENGTH = 150;
    private static final String DEFAULT_AVATAR_URL = "https://via.placeholder.com/56x56/6366f1/ffffff?text=U";

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(OTP_EMAIL_SUBJECT);
            helper.setText(buildOtpEmailContent(otp), true);

            javaMailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw e;
        }
    }

    @Async
    @Override
    public void sendNotificationEmail(String to, String subject, String message,
                                      String actorName, String actorProfilePicUrl,
                                      String postContent, String postUrl) throws MessagingException {

        CompletableFuture.runAsync(() -> {
            try {
                // Validate inputs
                if (!StringUtils.hasText(to) || !StringUtils.hasText(subject)) {
                    log.error("Invalid email parameters: to={}, subject={}", to, subject);
                    return;
                }

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

                helper.setFrom(fromEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(buildNotificationEmailContent(
                        message, actorName, actorProfilePicUrl, postContent, postUrl), true);

                javaMailSender.send(mimeMessage);
                log.info("Notification email sent successfully to: {} for actor: {}", to, actorName);

            } catch (Exception e) {
                log.error("Failed to send notification email to: {} for actor: {}", to, actorName, e);
            }
        });
    }

    private String buildNotificationEmailContent(String msg, String actorName,
                                                 String actorProfilePicUrl, String postContent, String postUrl) {

        // Sanitize inputs
        actorName = StringUtils.hasText(actorName) ? actorName : "Someone";
        actorProfilePicUrl = StringUtils.hasText(actorProfilePicUrl) ? actorProfilePicUrl : DEFAULT_AVATAR_URL;
        postUrl = StringUtils.hasText(postUrl) ? postUrl : "#";

        // Handle post content - if empty, we'll hide the preview section entirely
        boolean hasPostContent = StringUtils.hasText(postContent);
        String trimmedContent = "";
        if (hasPostContent) {
            trimmedContent = postContent.length() > MAX_POST_CONTENT_LENGTH
                    ? postContent.substring(0, MAX_POST_CONTENT_LENGTH) + "..."
                    : postContent;
        }

        String postPreviewHtml = "";
        String actorMarginBottom = "0"; // No margin if no post content

        if (hasPostContent) {
            actorMarginBottom = "24px"; // Add margin if there's post content
            postPreviewHtml = String.format("""
                            
                            <div class="post-preview">
                                %s
                            </div>""", trimmedContent);
        }

        return String.format("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <title>New Notification from Xplore</title>
            <!--[if mso]>
            <noscript>
                <xml>
                    <o:OfficeDocumentSettings>
                        <o:PixelsPerInch>96</o:PixelsPerInch>
                    </o:OfficeDocumentSettings>
                </xml>
            </noscript>
            <![endif]-->
            <style>
                /* Reset and base styles */
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
                    background: linear-gradient(135deg, #0f172a 0%%, #1e293b 50%%, #334155 100%%);
                    margin: 0;
                    padding: 0;
                    -webkit-font-smoothing: antialiased;
                    -moz-osx-font-smoothing: grayscale;
                    line-height: 1.6;
                }
                
                .email-wrapper {
                    background: linear-gradient(135deg, #0f172a 0%%, #1e293b 50%%, #334155 100%%);
                    padding: 40px 20px;
                    min-height: 100vh;
                }
                
                .container {
                    max-width: 600px;
                    margin: 0 auto;
                    background: #1e293b;
                    border-radius: 24px;
                    box-shadow: 0 20px 40px rgba(0,0,0,0.4), 0 8px 16px rgba(0,0,0,0.2);
                    overflow: hidden;
                    border: 1px solid #475569;
                }
                
                .header {
                    background: linear-gradient(135deg, #6366f1 0%%, #8b5cf6 50%%, #d946ef 100%%);
                    color: white;
                    padding: 40px 30px;
                    text-align: center;
                    position: relative;
                    overflow: hidden;
                }
                
                .header::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: radial-gradient(circle at 30%% 20%%, rgba(255,255,255,0.15) 0%%, transparent 50%%);
                    opacity: 0.8;
                }
                
                .header-content {
                    position: relative;
                    z-index: 2;
                }
                
                .header-icon {
                    font-size: 32px;
                    margin-bottom: 12px;
                    display: block;
                }
                
                .header h1 {
                    font-size: 32px;
                    font-weight: 700;
                    margin: 0 0 8px 0;
                    letter-spacing: -0.8px;
                }
                
                .header .subtitle {
                    font-size: 16px;
                    font-weight: 400;
                    opacity: 0.95;
                    letter-spacing: 0.2px;
                }
                
                .content {
                    padding: 48px 40px;
                    color: #e2e8f0;
                    line-height: 1.7;
                    font-size: 16px;
                }
                
                .notification-card {
                    background: linear-gradient(135deg, #374151 0%%, #4b5563 100%%);
                    border: 2px solid #475569;
                    border-radius: 20px;
                    padding: 32px;
                    margin: 32px 0;
                    position: relative;
                    transition: all 0.3s ease;
                    box-shadow: 0 8px 16px rgba(0,0,0,0.3);
                }
                
                .notification-card::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    height: 4px;
                    background: linear-gradient(90deg, #6366f1, #8b5cf6, #d946ef);
                    border-radius: 20px 20px 0 0;
                }
                
                .actor-info {
                    display: flex;
                    align-items: center;
                    margin-bottom: %s;
                }
                
                .avatar-container {
                    position: relative;
                    margin-right: 20px;
                    flex-shrink: 0;
                }
                
                .avatar {
                    width: 64px;
                    height: 64px;
                    border-radius: 50%%;
                    border: 4px solid #475569;
                    box-shadow: 0 8px 16px rgba(0,0,0,0.4);
                    object-fit: cover;
                    background: #64748b;
                }
                
   
                .actor-details {
                    flex: 1;
                    min-width: 0;
                }
                
                .actor-details h3 {
                    font-size: 20px;
                    font-weight: 600;
                    color: #f1f5f9;
                    margin: 0 0 6px 0;
                    letter-spacing: -0.4px;
                }
                
                .action-text {
                    font-size: 16px;
                    color: #94a3b8;
                    font-weight: 400;
                    line-height: 1.4;
                }
                
                .post-preview {
                    background: #1e293b;
                    border: 2px solid #475569;
                    border-radius: 16px;
                    padding: 24px;
                    margin: 24px 0 0 0;
                    font-style: normal;
                    color: #e2e8f0;
                    line-height: 1.7;
                    box-shadow: 0 4px 8px rgba(0,0,0,0.3);
                    position: relative;
                    font-size: 15px;
                }
                
                .post-preview::before {
                    content: '💬';
                    position: absolute;
                    top: -8px;
                    left: 20px;
                    background: #1e293b;
                    padding: 0 8px;
                    font-size: 18px;
                }
                
                .cta-section {
                    text-align: center;
                    margin: 40px 0 32px 0;
                }
                
                .cta-button {
                    display: inline-block;
                    padding: 18px 36px;
                    font-size: 16px;
                    font-weight: 600;
                    color: #ffffff !important;
                    background: linear-gradient(135deg, #6366f1 0%%, #8b5cf6 100%%);
                    border-radius: 16px;
                    text-decoration: none;
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.3);
                    border: 0;
                    cursor: pointer;
                    letter-spacing: 0.3px;
                    position: relative;
                    overflow: hidden;
                }
                
                .cta-button::before {
                    content: '';
                    position: absolute;
                    top: 0;
                    left: -100%%;
                    width: 100%%;
                    height: 100%%;
                    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
                    transition: left 0.5s;
                }
                
                .cta-button:hover::before {
                    left: 100%%;
                }
                
                .cta-button:hover {
                    transform: translateY(-3px);
                    box-shadow: 0 12px 30px rgba(99, 102, 241, 0.4);
                }
                
                .divider {
                    height: 2px;
                    background: linear-gradient(90deg, transparent 0%%, #475569 20%%, #64748b 50%%, #475569 80%%, transparent 100%%);
                    margin: 32px 0;
                    border-radius: 1px;
                }
                
                .unsubscribe {
                    text-align: center;
                    font-size: 14px;
                    color: #94a3b8;
                    line-height: 1.6;
                    margin: 24px 0;
                    padding: 20px;
                    background: #334155;
                    border-radius: 12px;
                    border: 1px solid #475569;
                }
                
                .unsubscribe a {
                    color: #a78bfa;
                    text-decoration: none;
                    font-weight: 500;
                    transition: color 0.3s ease;
                }
                
                .unsubscribe a:hover {
                    color: #c4b5fd;
                    text-decoration: underline;
                }
                
                .footer {
                    background: linear-gradient(135deg, #334155 0%%, #475569 100%%);
                    color: #94a3b8;
                    text-align: center;
                    padding: 32px 30px;
                    font-size: 14px;
                    border-top: 2px solid #475569;
                }
                
                .footer-logo {
                    font-weight: 700;
                    color: #a78bfa;
                    font-size: 24px;
                    margin-bottom: 16px;
                    letter-spacing: -0.5px;
                }
                
                .social-links {
                    margin: 20px 0 16px 0;
                }
                
                .social-links a {
                    display: inline-block;
                    margin: 0 12px;
                    padding: 12px;
                    border-radius: 50%%;
                    background: #475569;
                    box-shadow: 0 4px 8px rgba(0,0,0,0.3);
                    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                    text-decoration: none;
                    font-size: 18px;
                    border: 1px solid #64748b;
                }
                
                .social-links a:hover {
                    transform: translateY(-4px) scale(1.1);
                    box-shadow: 0 8px 16px rgba(0,0,0,0.4);
                    background: #64748b;
                }
                
                .footer-text {
                    margin: 8px 0;
                    font-weight: 500;
                    color: #cbd5e1;
                }
                
                .footer-tagline {
                    margin-top: 12px;
                    font-size: 13px;
                    opacity: 0.8;
                    font-style: italic;
                    color: #94a3b8;
                }
                
                /* Mobile responsiveness */
                @media only screen and (max-width: 600px) {
                    .email-wrapper {
                        padding: 20px 10px;
                    }
                    
                    .container {
                        border-radius: 20px;
                        margin: 0;
                    }
                    
                    .header {
                        padding: 32px 20px;
                    }
                    
                    .header h1 {
                        font-size: 28px;
                    }
                    
                    .content {
                        padding: 32px 24px;
                    }
                    
                    .notification-card {
                        padding: 24px 20px;
                        margin: 24px 0;
                    }
                    
                    .actor-info {
                        flex-direction: row;
                        text-align: left;
                    }
                    
                    .avatar {
                        width: 56px;
                        height: 56px;
                    }
                    
                    .avatar-container {
                        margin-right: 16px;
                    }
                    
                    .cta-button {
                        width: calc(100%% - 20px);
                        padding: 20px 24px;
                        margin: 0 10px;
                    }
                    
                    .social-links a {
                        margin: 0 8px;
                        padding: 10px;
                    }
                }
                
                /* High contrast mode support */
                @media (prefers-contrast: high) {
                    .container {
                        border: 2px solid #000000;
                    }
                    
                    .notification-card {
                        border: 2px solid #374151;
                    }
                    
                    .post-preview {
                        border: 2px solid #6b7280;
                    }
                }
                
                /* Reduced motion support */
                @media (prefers-reduced-motion: reduce) {
                    .cta-button,
                    .social-links a,
                    .notification-card {
                        transition: none;
                    }
                    
                    .cta-button:hover,
                    .social-links a:hover {
                        transform: none;
                    }
                }
            </style>
        </head>
        <body>
            <div class="email-wrapper">
                <div class="container">
                    <div class="header">
                        <div class="header-content">
                            <div class="header-icon">🎯</div>
                            <h1>New Activity</h1>
                            <div class="subtitle">Someone interacted with your content on Xplore</div>
                        </div>
                    </div>
                    
                    <div class="content">
                        <div class="notification-card">
                            <div class="actor-info">
                                <div class="avatar-container">
                                    <img src="%s" alt="%s's profile picture" class="avatar" 
                                         onerror="this.src='%s'">
                                </div>
                                <div class="actor-details">
                                    <h3>%s</h3>
                                    <div class="action-text">%s</div>
                                </div>
                            </div>
                            %s
                        </div>
                        
                        <div class="cta-section">
                            <a href="%s" class="cta-button">
                                View Full Post →
                            </a>
                        </div>
                        
                        <div class="divider"></div>
                        
                        <div class="unsubscribe">
                            <p><strong>Manage your notifications</strong></p>
                            <p>You're receiving this because you're subscribed to Xplore notifications.<br> To unsubscribe, you’ll need to change your notification settings.<br>
                            <a href="https://xplore-v7f1.vercel.app/notifications">Update notification preferences</a> • 
                            <a href="https://xplore-v7f1.vercel.app/notifications">Unsubscribe</a></p>
                        </div>
                    </div>
                    
                    <div class="footer">
                        <div class="footer-logo">Xplore</div>
                        <div class="social-links">
                            <a href="#" aria-label="Follow us on Twitter">🐦</a>
                            <a href="#" aria-label="Connect on LinkedIn">💼</a>
                            <a href="#" aria-label="Like us on Facebook">👍</a>
                            <a href="#" aria-label="Follow on Instagram">📷</a>
                        </div>
                        <div class="footer-text">© %d Xplore. All rights reserved.</div>
                        <div class="footer-tagline">Connecting minds, sharing ideas ✨</div>
                    </div>
                </div>
            </div>
        </body>
        </html>
        """,
                actorMarginBottom,
                actorProfilePicUrl,
                actorName,
                DEFAULT_AVATAR_URL, // Fallback image
                actorName,
                getMsg(msg),
                postPreviewHtml, // This will be empty string if no content
                postUrl,
                Year.now().getValue());
    }

    private String getMsg(String str) {
        if (!StringUtils.hasText(str)) {
            return "";
        }
        String[] parts = str.split("\\s+", 2);
        return (parts.length > 1) ? " " + parts[1] : "";
    }

    private String buildOtpEmailContent(String otp) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                        margin: 0;
                        padding: 40px 20px;
                        -webkit-font-smoothing: antialiased;
                    }
                    .container {
                        max-width: 500px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 24px;
                        overflow: hidden;
                        box-shadow: 0 20px 40px rgba(0,0,0,0.15);
                    }
                    .header {
                        background: linear-gradient(135deg, #10b981 0%%, #059669 100%%);
                        padding: 40px 30px;
                        text-align: center;
                        color: #ffffff;
                    }
                    .header h1 {
                        font-size: 28px;
                        font-weight: 700;
                        margin: 0;
                        letter-spacing: -0.5px;
                    }
                    .header .subtitle {
                        font-size: 16px;
                        opacity: 0.9;
                        margin-top: 8px;
                    }
                    .content {
                        padding: 40px 30px;
                        color: #374151;
                        line-height: 1.7;
                        text-align: center;
                    }
                    .otp-container {
                        background: linear-gradient(135deg, #f8fafc 0%%, #f1f5f9 100%%);
                        border: 2px solid #e2e8f0;
                        border-radius: 20px;
                        padding: 32px;
                        margin: 32px 0;
                        position: relative;
                    }
                    .otp-container::before {
                        content: '';
                        position: absolute;
                        top: 0;
                        left: 0;
                        right: 0;
                        height: 4px;
                        background: linear-gradient(90deg, #10b981, #059669);
                        border-radius: 20px 20px 0 0;
                    }
                    .otp-label {
                        font-size: 14px;
                        color: #6b7280;
                        margin-bottom: 16px;
                        font-weight: 500;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                    }
                    .otp-box {
                        background: #ffffff;
                        border: 3px solid #10b981;
                        padding: 20px;
                        font-size: 32px;
                        font-weight: 700;
                        letter-spacing: 8px;
                        margin: 0;
                        border-radius: 16px;
                        color: #1f2937;
                        font-family: 'Courier New', monospace;
                        box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
                    }
                    .expiry-note {
                        margin-top: 24px;
                        padding: 16px;
                        background: #fef3cd;
                        border: 1px solid #fbbf24;
                        border-radius: 12px;
                        color: #92400e;
                        font-size: 14px;
                        font-weight: 500;
                    }
                    .footer {
                        background: linear-gradient(135deg, #f8fafc 0%%, #f1f5f9 100%%);
                        padding: 24px 30px;
                        text-align: center;
                        font-size: 14px;
                        color: #6b7280;
                        border-top: 1px solid #e5e7eb;
                    }
                    .footer-logo {
                        font-weight: 700;
                        color: #10b981;
                        font-size: 20px;
                        margin-bottom: 8px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Verification Code</h1>
                        <div class="subtitle">Complete your Xplore registration</div>
                    </div>
                    <div class="content">
                        <p style="font-size: 18px; margin-bottom: 24px;">Hello! Welcome to <strong>Xplore</strong>.</p>
                        <p>Use the verification code below to complete your registration:</p>
                        
                        <div class="otp-container">
                            <div class="otp-label">Verification Code</div>
                            <div class="otp-box">%s</div>
                            <div class="expiry-note">
                                ⏰ <strong>Expires in 5 minutes</strong> for your security
                            </div>
                        </div>
                        
                        <p style="margin-top: 24px; font-size: 15px; color: #6b7280;">
                            If you didn't request this code, please ignore this email.<br>
                            Your account security is important to us.
                        </p>
                    </div>
                    <div class="footer">
                        <div class="footer-logo">Xplore</div>
                        <div>© %d Xplore. All rights reserved.</div>
                    </div>
                </div>
            </body>
            </html>
            """, otp, Year.now().getValue());
    }
}