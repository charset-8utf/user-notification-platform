package com.notification.metrics;

import com.notification.kafka.KafkaLagMonitor;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicLong;

@Configuration
@Profile("kafka")
@EnableScheduling
public class KafkaLagMetricsConfiguration {

    @Bean
    AtomicLong kafkaConsumerLagTotalHolder() {
        return new AtomicLong(0);
    }

    @Bean
    Gauge kafkaConsumerLagTotal(AtomicLong kafkaConsumerLagTotalHolder, MeterRegistry registry) {
        return Gauge.builder("app.kafka.consumer.lag.total", kafkaConsumerLagTotalHolder, holder -> (double) holder.get())
                .description("Total Kafka consumer lag for notification topic")
                .register(registry);
    }

    @Bean
    KafkaLagMetricsRefresher kafkaLagMetricsRefresher(
            KafkaLagMonitor lagMonitor,
            AtomicLong kafkaConsumerLagTotalHolder
    ) {
        return new KafkaLagMetricsRefresher(lagMonitor, kafkaConsumerLagTotalHolder);
    }

    static final class KafkaLagMetricsRefresher {

        private final KafkaLagMonitor lagMonitor;
        private final AtomicLong lagHolder;

        KafkaLagMetricsRefresher(KafkaLagMonitor lagMonitor, AtomicLong lagHolder) {
            this.lagMonitor = lagMonitor;
            this.lagHolder = lagHolder;
        }

        @Scheduled(fixedRateString = "${app.metrics.kafka-lag-refresh-ms:15000}")
        void refresh() {
            try {
                lagHolder.set(lagMonitor.measureLag().totalLag());
            } catch (Exception ignored) {
                // gauge keeps last value; health indicator reports errors
            }
        }
    }
}
