package com.rema.web_api.email;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendVerificationEmail() {
        String to = "user@example.com";
        String link = "http://localhost/verify?token=123";

        emailService.sendVerificationEmail(to, link);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(to, message.getTo()[0]);
        assertEquals("estatescoutcomp@gmail.com", message.getFrom());
        assertEquals("Potwierdź rejestrację", message.getSubject());
        assertEquals("Kliknij w link, aby potwierdzić rejestrację: " + link, message.getText());
    }

    @Test
    void sendPasswordResetEmail() {
        String to = "user@example.com";
        String link = "http://localhost/reset?token=abc";

        emailService.sendPasswordResetEmail(to, link);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(to, message.getTo()[0]);
        assertEquals("estatescoutcomp@gmail.com", message.getFrom());
        assertEquals("Resetowanie hasła", message.getSubject());
        assertEquals("Kliknij w link, aby zresetować hasło: " + link, message.getText());
    }
}
