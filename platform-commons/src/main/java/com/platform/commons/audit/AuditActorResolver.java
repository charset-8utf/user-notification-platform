package com.platform.commons.audit;

/**
 * Strategy: определение актора аудита (пользователь, сервис, anonymous).
 */
public interface AuditActorResolver {

    String resolveActor();
}
