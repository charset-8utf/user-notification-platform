package com.platform.kafka.contracts;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Общие свойства Confluent Avro serde для Kafka-клиентов.
 */
public final class KafkaAvroClientProperties {

    private KafkaAvroClientProperties() {
    }

    public static Map<String, Object> avroSerdeConfig(String schemaRegistryUrl) {
        Map<String, Object> config = new HashMap<>();
        config.put(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        config.put(AbstractKafkaSchemaSerDeConfig.AUTO_REGISTER_SCHEMAS, true);
        config.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
        return config;
    }
}
