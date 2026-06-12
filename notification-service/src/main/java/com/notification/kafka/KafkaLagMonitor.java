package com.notification.kafka;

import com.notification.config.kafka.NotificationKafkaProperties;
import com.notification.exception.KafkaLagMonitorException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.OffsetSpec;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Вычисляет consumer lag для метрик и health.
 */
@Component
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaLagMonitor {

    private static final long ADMIN_TIMEOUT_SEC = 5;

    private final KafkaAdmin kafkaAdmin;
    private final KafkaProperties kafkaProperties;
    private final NotificationKafkaProperties notificationKafkaProperties;

    public LagSnapshot measureLag() {
        String groupId = Objects.requireNonNull(
                kafkaProperties.getConsumer().getGroupId(),
                "spring.kafka.consumer.group-id должен быть задан");
        String topic = notificationKafkaProperties.topic();
        Map<String, Object> adminConfig = new HashMap<>(kafkaAdmin.getConfigurationProperties());
        adminConfig.putIfAbsent(
                org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafkaProperties.getBootstrapServers()
        );
        try (AdminClient admin = AdminClient.create(adminConfig)) {
            ListConsumerGroupOffsetsResult committedResult = admin.listConsumerGroupOffsets(groupId);
            Map<TopicPartition, OffsetAndMetadata> committed =
                    await(committedResult.partitionsToOffsetAndMetadata(), groupId, "зафиксированных offset'ов");

            long totalLag = 0;
            Map<String, Long> lagByPartition = new HashMap<>();
            for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : committed.entrySet()) {
                TopicPartition tp = entry.getKey();
                if (topic.equals(tp.topic())) {
                    var endOffsetInfo = await(
                            admin.listOffsets(Map.of(tp, OffsetSpec.latest())).all(),
                            groupId,
                            "конечных offset'ов").get(tp);
                    if (endOffsetInfo != null) {
                        long endOffset = endOffsetInfo.offset();
                        long consumerOffset = entry.getValue().offset();
                        long lag = Math.max(0, endOffset - consumerOffset);
                        totalLag += lag;
                        lagByPartition.put(tp.topic() + "-" + tp.partition(), lag);
                    }
                }
            }
            return new LagSnapshot(groupId, topic, totalLag, lagByPartition);
        }
    }

    private <T> T await(Future<T> future, String groupId, String operation) {
        try {
            return future.get(ADMIN_TIMEOUT_SEC, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new KafkaLagMonitorException(
                    "Прервано при запросе " + operation + " для consumer group " + groupId, ex);
        } catch (TimeoutException ex) {
            throw new KafkaLagMonitorException(
                    "Таймаут при запросе " + operation + " для consumer group " + groupId, ex);
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            throw new KafkaLagMonitorException(
                    "Не удалось получить " + operation + " для consumer group " + groupId, cause);
        }
    }

    public record LagSnapshot(String groupId, String topic, long totalLag, Map<String, Long> lagByPartition) {
    }
}
