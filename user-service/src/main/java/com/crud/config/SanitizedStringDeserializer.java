package com.crud.config;

import org.jspecify.annotations.Nullable;
import org.springframework.web.util.HtmlUtils;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

/**
 * Jackson 3 deserializer: HTML-escape string JSON values when {@link Sanitized#value()} is true.
 */
public class SanitizedStringDeserializer extends ValueDeserializer<String> {

    private final boolean sanitize;

    /**
     * Default constructor used by Jackson before {@link #createContextual}.
     */
    public SanitizedStringDeserializer() {
        this(true);
    }

    public SanitizedStringDeserializer(boolean sanitize) {
        this.sanitize = sanitize;
    }

    @Override
    public Class<String> handledType() {
        return String.class;
    }

    @Override
    public @Nullable String deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        String value = p.getValueAsString();
        if (value == null || !sanitize) {
            return value;
        }
        return HtmlUtils.htmlEscape(value, "UTF-8");
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
            throws JacksonException {
        boolean flag;
        Sanitized ann = property.getAnnotation(Sanitized.class);
        flag = ann.value();
        if (flag == sanitize) {
            return this;
        }
        return new SanitizedStringDeserializer(flag);
    }
}
