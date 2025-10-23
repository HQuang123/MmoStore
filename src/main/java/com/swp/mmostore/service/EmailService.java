package com.swp.mmostore.service;

import com.google.api.client.util.Value;
import com.swp.mmostore.util.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;
@Slf4j
@Service
public class EmailService {
    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 2000L;
    private static final long BACKOFF_MULTIPLIER = 2L;


    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${application.email.personalName:MMOStore}")
    private String personalName;

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
    // Public API: generic async email sender with retry
    @Async
    public void sendEmailAsync(String to, String subject, String htmlContent) {
        if (to == null || to.isBlank()) {
            log.warn("Skip sending email: missing recipient (subject={})", subject);
            return;
        }
        if (mailSender == null) {
            log.error("JavaMailSender bean is null - cannot send email to {} (subject={}). Check mail configuration.", to, subject);
            return;
        }

        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                sendEmail(to, subject, htmlContent);
                log.info("Email sent successfully to {} (subject={}) on attempt {}", to, subject, attempt);
                lastException = null;
                break;
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} to send email to {} (subject={}) failed: {}", attempt, to, subject, e.toString());
                if (attempt == MAX_RETRIES) {
                    // fall through to final log below
                    break;
                }
                try {
                    Thread.sleep(BASE_DELAY_MS * (long) Math.pow(BACKOFF_MULTIPLIER, attempt - 1));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Email retry interrupted while sending to {} (subject={})", to, subject, ie);
                    lastException = ie;
                    break;
                }
            }
        }
        if (lastException != null) {
            log.error("Failed to send email to {} (subject={}) after {} attempts", to, subject, MAX_RETRIES, lastException);
        }
    }

    //send verification code
    public void sendVerificationCodeEmailAsync(String to, String code) {
        String subject = "MMOMarket - Xác thực tài khoản & Đổi mật khẩu";
        String content = EmailTemplate.verificationEmail(code);
        sendEmailAsync(to, subject, content);
    }

    // Core send method
    private void sendEmailUpdate(String to, String subject, String htmlContent) throws MessagingException {
        if (mailSender == null) {
            throw new IllegalStateException("JavaMailSender not configured");
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        String fromAddr = resolveFromAddress();
        try {
            helper.setFrom(fromAddr, personalName);
        } catch (Exception e) {
            // Fallback: set without personal name
            helper.setFrom(fromAddr);
        }
        helper.setTo(to);
        // Always CC to anh.tuan662005@gmail.com
        helper.setCc("huynguyensteph@gmail.com");
        helper.setCc("shirohoang0305@gmail.com");
        helper.setCc("ducanhschlain@gmail.com");
//        helper.setCc("@gmail.com");
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private String resolveFromAddress() {
        if (mailUsername != null && !mailUsername.isBlank()) {
            return mailUsername.trim();
        }
        return "no-reply@mmostore.com";
    }


}
