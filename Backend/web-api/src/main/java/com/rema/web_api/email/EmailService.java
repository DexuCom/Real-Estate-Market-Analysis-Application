package com.rema.web_api.email;

import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender _mailSender) {
        this.mailSender = _mailSender;
    }

    @Transactional
    public void sendVerificationEmail(String to, String verificationLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Potwierdź rejestrację");
        message.setText("Kliknij w link, aby potwierdzić rejestrację: " + verificationLink);

        mailSender.send(message);
    }

    @Transactional
    public void sendPasswordResetEmail(String to, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject("Resetowanie hasła");
        message.setText("Kliknij w link, aby zresetować hasło: " + resetLink);

        mailSender.send(message);
    }
}
