package com.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Декларативное создание Kafka-топиков. KafkaAdmin создаёт топики, если их ещё нет
 * (на брокерах с {@code auto.create.topics.enable=true} это страховка; на проде, где
 * автосоздание выключено, — основной способ).
 */
@Configuration
@Profile("kafka")
public class KafkaTopicsConfig {

    @Bean
    public NewTopic userNotificationsTopic(
            @Value("${app.notification.kafka.topic}") String topic,
            @Value("${app.notification.kafka.partitions:1}") int partitions,
            @Value("${app.notification.kafka.replicas:1}") short replicas
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicas).build();
    }

    @Bean
    public NewTopic userNotificationsDltTopic(
            @Value("${app.notification.kafka.topic}") String topic,
            @Value("${app.notification.kafka.dlt-suffix:.DLT}") String dltSuffix,
            @Value("${app.notification.kafka.partitions:1}") int partitions,
            @Value("${app.notification.kafka.replicas:1}") short replicas
    ) {
        return TopicBuilder.name(topic + dltSuffix).partitions(partitions).replicas(replicas).build();
    }
}
