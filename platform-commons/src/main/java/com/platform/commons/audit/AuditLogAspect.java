package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Aspect
@Component
@ConditionalOnClass(name = "org.aspectj.lang.ProceedingJoinPoint")
public class AuditLogAspect {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        return AuditLogInvocationProxy.invokeWithAudit(joinPoint, auditLog, this::logSuccess);
    }

    private void logSuccess(AuditLog auditLog, ProceedingJoinPoint joinPoint, Object result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "audit");
        payload.put("action", auditLog.action());
        payload.put("resourceType", auditLog.resourceType());
        payload.put("actor", resolveActor());
        payload.put("method", methodName(joinPoint));
        payload.put("traceId", MDC.get("traceId"));
        payload.put("spanId", MDC.get("spanId"));
        if (result != null) {
            payload.put("resultType", result.getClass().getSimpleName());
        }
        AUDIT.info("{}", payload);
    }

    private static String resolveActor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .orElse("anonymous");
    }

    private static String methodName(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        }
        return joinPoint.getSignature().toShortString();
    }
}
