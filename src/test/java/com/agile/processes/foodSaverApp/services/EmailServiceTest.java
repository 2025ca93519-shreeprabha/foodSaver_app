package com.agile.processes.foodSaverApp.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    void sendEmailWithAttachment_Success() throws Exception {
        byte[] attachmentBytes = new byte[]{1, 2, 3};

        emailService.sendEmailWithAttachment("test@test.com", "Subject", "Body", attachmentBytes, "test.pdf");

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendEmailWithAttachment_ExceptionHandled() throws Exception {
        doThrow(new org.springframework.mail.MailSendException("Mail exception")).when(javaMailSender).send(any(MimeMessage.class));

        byte[] attachmentBytes = new byte[]{1, 2, 3};

        // Spring's MailException is a RuntimeException, so it bubbles up since the service only catches MessagingException
        assertThrows(org.springframework.mail.MailException.class, () -> {
            emailService.sendEmailWithAttachment("test@test.com", "Subject", "Body", attachmentBytes, "test.pdf");
        });

        verify(javaMailSender, times(1)).send(mimeMessage);
    }
}
