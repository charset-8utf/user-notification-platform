package com.crud.notification.kafka;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonJsonSerializerKafkaTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void serializeReturnsNullForNullPayload() {
        try (JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(jsonMapper).noTypeInfo()) {
            assertThat(serializer.serialize("user-notifications", null)).isNull();
        }
    }

    @Test
    void serializeProducesValidJsonForEvent() {
        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_CREATED, "user@example.com");

        try (JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(jsonMapper).noTypeInfo()) {
            byte[] bytes = serializer.serialize("user-notifications", event);
            assertThat(bytes).isNotEmpty();
            assertThat(new String(bytes))
                    .contains("\"operation\":\"USER_CREATED\"")
                    .contains("\"eventId\"")
                    .contains("\"email\":\"user@example.com\"")
                    .doesNotContain("__TypeId__");
        }
    }

    @Test
    void serializeWrapsJacksonErrorIntoSerializationException() {
        JsonMapper mapper = Mockito.mock(JsonMapper.class);
        Mockito.when(mapper.writeValueAsBytes(Mockito.any()))
                .thenThrow(new JacksonException("boom") {});

        try (JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(mapper).noTypeInfo()) {
            Object payload = new Object();
            assertThatThrownBy(() -> serializer.serialize("user-notifications", payload))
                    .isInstanceOf(SerializationException.class);
        }
    }
}
