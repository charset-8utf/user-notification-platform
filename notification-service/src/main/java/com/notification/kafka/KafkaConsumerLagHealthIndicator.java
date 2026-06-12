package com.notification.kafka;

import com.notification.exception.KafkaLagMonitorException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Actuator health: суммарный lag consumer group.
 */
@Component("kafkaConsumerLag")
@Profile("kafka")
@RequiredArgsConstructor
public class KafkaConsumerLagHealthIndicator implements HealthIndicator {

    private static final String DETAIL_GROUP_ID = "groupId";

    private final KafkaLagMonitor lagMonitor;

    @Override
    public Health health() {
        try {
            KafkaLagMonitor.LagSnapshot snapshot = lagMonitor.measureLag();
            return Health.up()
                    .withDetail(DETAIL_GROUP_ID, snapshot.groupId())
                    .withDetail("topic", snapshot.topic())
                    .withDetail("totalLag", snapshot.totalLag())
                    .withDetail("lagByPartition", snapshot.lagByPartition())
                    .build();
        } catch (KafkaLagMonitorException ex) {
            return Health.down()
                    .withException(ex)
                    .build();
        }
    }
}
