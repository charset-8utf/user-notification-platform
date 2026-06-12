package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * Strategy: форматирование имени audited-метода для лога.
 */
public interface AuditMethodNameResolver {

    String resolve(ProceedingJoinPoint joinPoint);
}
