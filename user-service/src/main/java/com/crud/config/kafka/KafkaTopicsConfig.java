package com.crud.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Декларирует Kafka-топики при активном профиле {@code kafka}.
 * Один из сервисов создаёт топик, остальные подключаются к существующему.
 */
@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "app.kafka.security.enabled", havingValue = "false", matchIfMissing = true)
public class KafkaTopicsConfig {

    private final UserNotificationKafkaProperties notificationKafkaProperties;

    public KafkaTopicsConfig(UserNotificationKafkaProperties notificationKafkaProperties) {
        this.notificationKafkaProperties = notificationKafkaProperties;
    }

    @Bean
    public NewTopic userNotificationsTopic() {
        return TopicBuilder.name(notificationKafkaProperties.topic())
                .partitions(notificationKafkaProperties.partitions())
                .replicas(notificationKafkaProperties.replicas())
                .build();
    }

    @Bean
    public NewTopic notificationCompensationsTopic() {
        return TopicBuilder.name(notificationKafkaProperties.compensationTopic())
                .partitions(notificationKafkaProperties.partitions())
                .replicas(notificationKafkaProperties.replicas())
                .build();
    }
}
