package com.platform.commons.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * AOP-обёртка: делегирует вызов методу через {@link AuditLogInvocationProxy} (Proxy).
 */
@Aspect
@Component
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogWriter auditLogWriter;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        return AuditLogInvocationProxy.invokeWithAudit(joinPoint, auditLog, auditLogWriter);
    }
}
