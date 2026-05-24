package com.notification.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Вычисляет consumer lag для метрик и health.
 */
@Component
@Profile("kafka")
public class KafkaLagMonitor {

    private static final long ADMIN_TIMEOUT_SEC = 5;

    private final KafkaAdmin kafkaAdmin;
    private final KafkaProperties kafkaProperties;
    private final String groupId;
    private final String topic;

    public KafkaLagMonitor(
            KafkaAdmin kafkaAdmin,
            KafkaProperties kafkaProperties,
            @Value("${spring.kafka.consumer.group-id}") String groupId,
            @Value("${app.notification.kafka.topic}") String topic
    ) {
        this.kafkaAdmin = kafkaAdmin;
        this.kafkaProperties = kafkaProperties;
        this.groupId = groupId;
        this.topic = topic;
    }

    public LagSnapshot measureLag() throws Exception {
        Map<String, Object> adminConfig = new HashMap<>(kafkaAdmin.getConfigurationProperties());
        adminConfig.putIfAbsent(
                org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()
        );
        try (AdminClient admin = AdminClient.create(adminConfig)) {
            ListConsumerGroupOffsetsResult committedResult = admin.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> committed =
                    committedResult.partitionsToOffsetAndMetadata().get(ADMIN_TIMEOUT_SEC, TimeUnit.SECONDS);

            long totalLag = 0;
            Map<String, Long> lagByPartition = new HashMap<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : committed.entrySet()) {
                TopicPartition tp = entry.getKey();
                if (!topic.equals(tp.topic())) {
                    continue;
                }
                long endOffset = admin.listOffsets(Map.of(tp, OffsetSpec.latest()))
                        .all()
                        .get(ADMIN_TIMEOUT_SEC, TimeUnit.SECONDS)
                        .get(tp)
                        .offset();
                long consumerOffset = entry.getValue().offset();
                long lag = Math.max(0, endOffset - consumerOffset);
                totalLag += lag;
                lagByPartition.put(tp.topic() + "-" + tp.partition(), lag);
            }
            return new LagSnapshot(groupId, topic, totalLag, lagByPartition);
        }
    }

    public record LagSnapshot(String groupId, String topic, long totalLag, Map<String, Long> lagByPartition) {
    }
}
