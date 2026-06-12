package com.platform.kafka.contracts;

import com.platform.kafka.avro.NotificationEmailMessage;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.function.Function;

public class NotificationEmailAvroDeserializer<T> implements Deserializer<T> {

    private final KafkaAvroDeserializer delegate = new KafkaAvroDeserializer();
    private final Function<NotificationEmailMessage, T> mapper;

    public NotificationEmailAvroDeserializer(Function<NotificationEmailMessage, T> mapper) {
        this.mapper = mapper;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        delegate.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }
        Object raw = delegate.deserialize(topic, data);
        if (raw == null) {
            return null;
        }
        if (!(raw instanceof NotificationEmailMessage message)) {
            throw new IllegalStateException(
                    "Ожидался NotificationEmailMessage, получен " + raw.getClass().getName());
        }
        return mapper.apply(message);
    }

    @Override
    public void close() {
        delegate.close();
    }
}
