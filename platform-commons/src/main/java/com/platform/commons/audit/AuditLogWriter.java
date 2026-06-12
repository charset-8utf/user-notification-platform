package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface AuditLogWriter {

    void writeSuccess(AuditLog auditLog, ProceedingJoinPoint joinPoint, @Nullable Object result);
}
