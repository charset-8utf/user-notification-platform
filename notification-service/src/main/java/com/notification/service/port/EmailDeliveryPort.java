package com.notification.service.port;

/**
 * Порт отправки email (Adapter: {@link com.notification.delivery.JavaMailEmailDeliveryAdapter}).
 */
public interface EmailDeliveryPort {

    void send(String from, String to, String subject, String body);
}
