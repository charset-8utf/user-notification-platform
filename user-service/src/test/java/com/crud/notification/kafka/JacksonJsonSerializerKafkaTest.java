package com.crud.notification.kafka;

import com.crud.notification.UserNotificationEvent;
import com.crud.notification.UserNotificationOperation;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectWriter;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JacksonJsonSerializerKafkaTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void serializeReturnsNullForNullPayload() {
        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(jsonMapper).noTypeInfo();

        assertThat(serializer.serialize("user-notifications", null)).isNull();
    }

    @Test
    void serializeProducesValidJsonForEvent() {
        JacksonJsonSerializer<UserNotificationEvent> serializer =
                new JacksonJsonSerializer<UserNotificationEvent>(jsonMapper).noTypeInfo();
        UserNotificationEvent event = UserNotificationEvent.create(
                UserNotificationOperation.USER_CREATED, "user@example.com");

        byte[] bytes = serializer.serialize("user-notifications", event);

        assertThat(bytes).isNotEmpty();
        String json = new String(bytes);
        assertThat(json).contains("\"operation\":\"USER_CREATED\"");
        assertThat(json).contains("\"eventId\"");
        assertThat(json).contains("\"email\":\"user@example.com\"");
    }

    @Test
    void serializeWrapsJacksonErrorIntoSerializationException() throws Exception {
        JsonMapper mapper = Mockito.mock(JsonMapper.class);
        ObjectWriter writer = Mockito.mock(ObjectWriter.class);
        Mockito.when(mapper.writerFor(ArgumentMatchers.<JavaType>isNull())).thenReturn(writer);
        Mockito.when(writer.writeValueAsBytes(Mockito.any())).thenThrow(new JacksonException("boom") {});

        JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>(mapper).noTypeInfo();

        assertThatThrownBy(() -> serializer.serialize("user-notifications", new Object()))
                .isInstanceOf(SerializationException.class)
                .hasMessageContaining("user-notifications")
                .hasCauseInstanceOf(JacksonException.class);
    }
}
