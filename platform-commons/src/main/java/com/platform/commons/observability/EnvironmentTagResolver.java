package com.platform.commons.observability;

/**
 * Strategy: значение тега {@code environment} для Micrometer common tags.
 */
public interface EnvironmentTagResolver {

    String resolve();
}
