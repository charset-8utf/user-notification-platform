package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;

@FunctionalInterface
public interface AuditLogWriter {

    void writeSuccess(AuditLog auditLog, ProceedingJoinPoint joinPoint, Object result);
}
