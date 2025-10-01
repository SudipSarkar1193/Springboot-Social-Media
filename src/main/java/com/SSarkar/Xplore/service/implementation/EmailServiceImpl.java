package com.SSarkar.Xplore.service.implementation;

import brevoApi.TransactionalEmailsApi;
import brevoModel.SendSmtpEmail;
import brevoModel.SendSmtpEmailSender;
import brevoModel.SendSmtpEmailTo;
import com.SSarkar.Xplore.service.contract.EmailService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private static final String OTP_EMAIL_SUBJECT = "Your OTP for Xplore Registration";
    private static final String DEFAULT_AVATAR_URL = "https://res.cloudinary.com/dvsutdpx2/image/upload/v1732181213/ryi6ouf4e0mwcgz1tcxx.png";
    private static final int MAX_POST_CONTENT_LENGTH = 150;
    private final TransactionalEmailsApi transactionalEmailsApi;

    @Value("${brevo.sender.email}")
    private String fromEmail;

    @Value("${brevo.sender.name}")
    private String fromName;

    @Override
    public void sendOtpEmail(String to, String otp) throws MessagingException {
        if (!StringUtils.hasText(to)) {
            log.error("Recipient email is empty");
            return;
        }

        SendSmtpEmailSender sender = new SendSmtpEmailSender()
                .email(fromEmail)
                .name(fromName);

        SendSmtpEmailTo recipient = new SendSmtpEmailTo()
                .email(to);

        SendSmtpEmail email = new SendSmtpEmail()
                .sender(sender)
                .to(Collections.singletonList(recipient))
                .subject(OTP_EMAIL_SUBJECT)
                .htmlContent(buildOtpEmailContent(otp));

        try {
            transactionalEmailsApi.sendTransacEmail(email);
            log.info("OTP email sent successfully to {}", to);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {} via Brevo API: {}", to, e.getMessage(), e);
            throw new MessagingException("Failed to send email via Brevo API", e);
        }
    }

    @Async
    @Override
    public void sendNotificationEmail(String to, String subject, String message,
                                      String actorName, String actorProfilePicUrl,
                                      String postContent, String postUrl) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject)) {
            log.error("Invalid email parameters: to={}, subject={}", to, subject);
            return;
        }

        SendSmtpEmailSender sender = new SendSmtpEmailSender()
                .email(fromEmail)
                .name(fromName);

        SendSmtpEmailTo recipient = new SendSmtpEmailTo()
                .email(to);

        SendSmtpEmail email = new SendSmtpEmail()
                .sender(sender)
                .to(Collections.singletonList(recipient))
                .subject(subject)
                .htmlContent(buildNotificationEmailContent(message, actorName, actorProfilePicUrl, postContent, postUrl));

        try {
            transactionalEmailsApi.sendTransacEmail(email);
            log.info("Notification email sent successfully to {} for actor {}", to, actorName);
        } catch (Exception e) {
            log.error("Failed to send notification email to {} for actor {} via Brevo API: {}", to, actorName, e.getMessage(), e);
        }
    }

    private String buildNotificationEmailContent(String msg, String actorName,
                                                 String actorProfilePicUrl, String postContent, String postUrl) {

        // --- 1. Sanitize Inputs ---
        actorName = StringUtils.hasText(actorName) ? actorName : "Someone";
        actorProfilePicUrl = StringUtils.hasText(actorProfilePicUrl) ? actorProfilePicUrl : DEFAULT_AVATAR_URL;
        postUrl = StringUtils.hasText(postUrl) ? postUrl : "#";

        // --- 2. Prepare and Escape User-Generated Content ---
        // This is the key fix: escape '%' in user content to prevent format errors.
        String actionText = getMsg(msg).replace("%", "%%");

        String postPreviewHtml = "";
        String actorMarginBottom = "0"; // No margin if there's no post content to show.
        boolean hasPostContent = StringUtils.hasText(postContent);

        if (hasPostContent) {
            actorMarginBottom = "24px"; // Add margin to separate actor info from the post preview.
            String trimmedContent = postContent.length() > MAX_POST_CONTENT_LENGTH
                    ? postContent.substring(0, MAX_POST_CONTENT_LENGTH) + "..."
                    : postContent;

            // Also escape the post content itself.
            String escapedTrimmedContent = trimmedContent.replace("%", "%%");

            postPreviewHtml = String.format("""
<div class="post-preview">%s</div>
""", escapedTrimmedContent);
        }

        String seeFullContentHtml = "";
        // Only show the "View Full Post" button for certain notifications (e.g., comments, not likes).
        if (!msg.toLowerCase().contains("follow")) {
            seeFullContentHtml = String.format("""
            <div class="cta-section">
                <a href="%s" class="cta-button">
                    View Full Post →
                </a>
            </div>
        """, postUrl);
        }

        // --- 3. Format the Final Email ---
        return String.format("""
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>New Notification from Xplore</title>
        <style>
            /* Reset and base styles */
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
                background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%);
                margin: 0;
                padding: 0;
                -webkit-font-smoothing: antialiased;
                -moz-osx-font-smoothing: grayscale;
                line-height: 1.6;
            }
            a{
                text-decoration: none;
            }
            .email-wrapper {
                background: linear-gradient(135deg, #f5f7fa 0%%, #c3cfe2 100%%);
                padding: 40px 20px;
                min-height: 100vh;
            }
            .container {
                max-width: 600px;
                margin: 0 auto;
                background: #ffffff;
                border-radius: 24px;
                box-shadow: 0 20px 40px rgba(0,0,0,0.1), 0 8px 16px rgba(0,0,0,0.05);
                overflow: hidden;
                border: 1px solid #dee2e6;
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
                color: #343a40;
                line-height: 1.7;
                font-size: 16px;
            }
            .notification-card {
                background: #f8f9fa;
                border: 1px solid #e9ecef;
                border-radius: 20px;
                padding: 32px;
                margin: 32px 0;
                position: relative;
                transition: all 0.3s ease;
                box-shadow: 0 8px 16px rgba(0,0,0,0.05);
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
            .avatar{
                 width: 64px;
                 height: 64px;
                 border-radius: 50%%;
                 border: 4px solid #ffffff;
                 box-shadow: 0 8px 16px rgba(0,0,0,0.1);
                 object-fit: cover;
                 background: transparent;
                 background-color: transparent ;
            }
            .actor-details {
                flex: 1;
                min-width: 0;
            }
            .actor-details h3 {
                font-size: 20px;
                font-weight: 600;
                color: #0055CC;
                margin: 0 0 6px 0;
                letter-spacing: -0.4px;
            }
            .actor-details h3 a {
                 color: #222222;
                 text-decoration: none;
                 font-weight: 600;
            }
            .action-text {
                font-size: 16px;
                color: #6c757d;
                font-weight: 400;
                line-height: 1.4;
            }
            .post-preview {
                background: #ffffff;
                border: 1px solid #dee2e6;
                border-radius: 16px;
                padding: 24px;
                margin: 24px 0 0 0;
                color: #495057;
                line-height: 1.7;
                box-shadow: 0 4px 8px rgba(0,0,0,0.05);
                position: relative;
                font-size: 15px;
                white-space: pre-wrap;
                word-wrap: break-word;
            }
            .post-preview::before {
                content: '💬';
                position: absolute;
                top: -12px;
                left: 20px;
                background: #f8f9fa;
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
                height: 1px;
                background: #e9ecef;
                margin: 32px 0;
                border-radius: 1px;
            }
            .unsubscribe {
                text-align: center;
                font-size: 14px;
                color: #6c757d;
                line-height: 1.6;
                margin: 24px 0;
                padding: 20px;
                background: #f1f3f5;
                border-radius: 12px;
                border: 1px solid #dee2e6;
            }
            .unsubscribe a {
                color: #6366f1;
                text-decoration: none;
                font-weight: 500;
                transition: color 0.3s ease;
            }
            .unsubscribe a:hover {
                color: #4346d8;
                text-decoration: underline;
            }
            .footer {
                background: #f8f9fa;
                color: #6c757d;
                text-align: center;
                padding: 32px 30px;
                font-size: 14px;
                border-top: 1px solid #dee2e6;
            }
            .footer-logo {
                font-weight: 700;
                color: #6366f1;
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
                background: #ffffff;
                box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                text-decoration: none;
                font-size: 18px;
                border: 1px solid #dee2e6;
            }
            .social-links a:hover {
                transform: translateY(-4px) scale(1.1);
                box-shadow: 0 8px 16px rgba(0,0,0,0.15);
                background: #f1f3f5;
            }
            .footer-text {
                margin: 8px 0;
                font-weight: 500;
                color: #495057;
            }
            .footer-tagline {
                margin-top: 12px;
                font-size: 13px;
                opacity: 0.8;
                font-style: italic;
                color: #6c757d;
            }

            /* Dark mode support */
            @media (prefers-color-scheme: dark) {
                body, .email-wrapper {
                    background: linear-gradient(135deg, #0f172a 0%%, #1e293b 50%%, #334155 100%%);
                    color: #111111;
                }
                .container {
                    background: #1e293b;
                    border: 1px solid #475569;
                    color: #e2e8f0;
                }
                .content { color: #e2e8f0; }
                .notification-card {
                    background: linear-gradient(135deg, #374151 0%%, #4b5563 100%%);
                    border: 1px solid #475569;
                }
                .avatar {
                    border-color: #475569;
                    background: transparent;
                    filter: none !important;
                }
                .actor-details h3 { color: #f1f5f9; }
                .actor-details h3 a { color: #f1f5f9 ; }
                .action-text { color: #94a3b8; }
                .post-preview {
                    background: #374151;
                    border: 1px solid #475569;
                    color: #cbd5e1;
                }
                .post-preview::before { background: #374151; }
                .divider { background: #475569; }
                .unsubscribe {
                    background: #334155;
                    border-color: #475569;
                    color: #94a3b8;
                }
                .unsubscribe a { color: #a78bfa; }
                .unsubscribe a:hover { color: #c4b5fd; }
                .footer {
                    background: #334155;
                    border-top-color: #475569;
                    color: #94a3b8;
                }
                .social-links a {
                    background: #475569;
                    border-color: #64748b;
                }
                .footer-text { color: #cbd5e1; }
                .footer-tagline { color: #94a3b8; }
            }
    
            /* Mobile responsiveness */
            @media only screen and (max-width: 600px) {
                .email-wrapper { padding: 20px 10px; }
                .container { border-radius: 20px; margin: 0; }
                .header { padding: 32px 20px; }
                .header h1 { font-size: 28px; }
                .content { padding: 32px 24px; }
                .notification-card { padding: 24px 20px; margin: 24px 0; }
                .avatar { width: 56px; height: 56px; }
                .avatar-container { margin-right: 16px; }
                .cta-button { width: calc(100%% - 20px); padding: 20px 24px; margin: 0 10px; }
                .social-links a { margin: 0 8px; padding: 10px; }
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
                            <a href="https://xplore-v7f1.vercel.app/profile/%s">
                            <div class="avatar-container">
                                <img src="%s" alt="%s's profile picture" class="avatar"
                                     style="background-color: transparent; display: block; border: none; outline: none; text-decoration: none; -webkit-filter: none; filter: none; color: #ffffff; mix-blend-mode: normal !important;"
                                     onerror="this.src='%s'">
                            </div>
                            </a>
                            <div class="actor-details">
                                <h3><a href="https://xplore-v7f1.vercel.app/profile/%s"><span>@</span>%s</a></h3>
                                <div class="action-text">%s</div>
                            </div>
                        </div>
                        %s</div>
                    
                    %s<div class="divider"></div>
                    
                    <div class="unsubscribe">
                        <p><strong>Manage your notifications</strong></p>
                        <p>You're receiving this because you're subscribed to Xplore notifications.<br>
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
                // Arguments in order, matching the specifiers:
                actorMarginBottom,       // 1. For actor-info margin
                actorName,               // 2. For profile link
                actorProfilePicUrl,      // 2. For avatar src
                actorName,               // 3. For avatar alt text
                DEFAULT_AVATAR_URL,      // 4. For avatar onerror fallback
                actorName,               // 5. For the heading name
                actorName,               // 6. For the profile link and display name
                actionText,              // 7. For the notification message (escaped)
                postPreviewHtml,         // 8. For the optional post preview (already contains escaped content)
                seeFullContentHtml,      // 9. For the optional CTA button
                Year.now().getValue()    // 10. For the copyright year
        );
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