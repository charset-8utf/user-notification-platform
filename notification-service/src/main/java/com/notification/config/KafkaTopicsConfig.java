package com.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean
    public NewTopic notificationCompensationsTopic(
            @Value("${app.notification.kafka.compensation-topic}") String topic,
            @Value("${app.notification.kafka.partitions:1}") int partitions,
            @Value("${app.notification.kafka.replicas:1}") short replicas
    ) {
        return TopicBuilder.name(topic).partitions(partitions).replicas(replicas).build();
    }
}
