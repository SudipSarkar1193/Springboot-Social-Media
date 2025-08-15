package com.SSarkar.Xplore.service.implementation;
import com.SSarkar.Xplore.service.contract.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {


    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String to, String otp) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Your OTP for Xplore Registration");
        helper.setText("Your OTP is: <b>" + otp + "</b>", true);
        javaMailSender.send(message);
    }
}
