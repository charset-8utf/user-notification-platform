package com.notification.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@Profile("kafka")
@ConditionalOnProperty(name = "app.kafka.security.enabled", havingValue = "false", matchIfMissing = true)
public class KafkaTopicsConfig {

    @Bean
    public NewTopic userNotificationsTopic(NotificationKafkaProperties kafka) {
        return TopicBuilder.name(kafka.topic())
                .partitions(kafka.partitions())
                .replicas(kafka.replicas())
                .build();
    }

    @Bean
    public NewTopic userNotificationsDltTopic(NotificationKafkaProperties kafka) {
        return TopicBuilder.name(kafka.topic() + kafka.dltSuffix())
                .partitions(kafka.partitions())
                .replicas(kafka.replicas())
                .build();
    }

    @Bean
    public NewTopic notificationCompensationsTopic(NotificationKafkaProperties kafka) {
        return TopicBuilder.name(kafka.compensationTopic())
                .partitions(kafka.partitions())
                .replicas(kafka.replicas())
                .build();
    }
}
