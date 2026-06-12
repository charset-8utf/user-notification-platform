package com.notification.delivery;

import com.notification.exception.EmailDeliveryException;
import com.notification.service.port.EmailDeliveryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Отправка email через Spring JavaMailSender.
 */
@Component
@RequiredArgsConstructor
public class JavaMailEmailDeliveryAdapter implements EmailDeliveryPort {

    private final JavaMailSender mailSender;

    @Override
    public void send(String from, String to, String subject, String body) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailDeliveryException("Не удалось отправить письмо", ex);
        }
    }
}
