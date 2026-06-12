package com.notification.kafka;

import com.notification.dto.NotificationEmailRequest;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile("kafka")
public class DltExceptionMessageExtractor {

    private static final String DEFAULT_MESSAGE =
            "ошибка доставки (заголовок DLT exception отсутствует)";

    public String extract(ConsumerRecord<String, NotificationEmailRequest> consumerRecord) {
        Header header = consumerRecord.headers().lastHeader(KafkaHeaders.DLT_EXCEPTION_MESSAGE);
        if (header == null || header.value() == null) {
            return DEFAULT_MESSAGE;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }
}
