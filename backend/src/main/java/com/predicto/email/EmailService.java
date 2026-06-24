package com.predicto.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendWelcomeEmail(String to, String username) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom("predictoSK@proton.me");
            msg.setTo(to);
            msg.setSubject("Vitaj na Predicto!");
            msg.setText("""
                Ahoj %s,

                Vitaj na Predicto — komunitnej platforme pre esportové predikcie!

                Tvoj účet bol úspešne vytvorený.
                Používateľské meno: %s

                Začni tipovať na: http://predicto.org

                Ak si neregistroval tento účet, ignoruj tento email.

                Tím Predicto
                predictoSK@proton.me
                """.formatted(username, username));
            mailSender.send(msg);
            log.info("Welcome email sent to {}", to);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", to, e.getMessage());
        }
    }
}
