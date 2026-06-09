package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;

public final class AuditLogInvocationProxy {

    private AuditLogInvocationProxy() {
    }

    public static Object invokeWithAudit(ProceedingJoinPoint joinPoint, AuditLog auditLog, AuditLogWriter writer)
            throws Throwable {
        Object result = joinPoint.proceed();
        writer.writeSuccess(auditLog, joinPoint, result);
        return result;
    }
}
