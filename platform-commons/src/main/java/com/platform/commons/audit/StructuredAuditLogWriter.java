package com.platform.commons.audit;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StructuredAuditLogWriter implements AuditLogWriter {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    private final AuditActorResolver actorResolver;
    private final AuditMethodNameResolver methodNameResolver;

    @Override
    public void writeSuccess(AuditLog auditLog, ProceedingJoinPoint joinPoint, @Nullable Object result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "audit");
        payload.put("action", auditLog.action());
        payload.put("resourceType", auditLog.resourceType());
        payload.put("actor", actorResolver.resolveActor());
        payload.put("method", methodNameResolver.resolve(joinPoint));
        payload.put("traceId", MDC.get("traceId"));
        payload.put("spanId", MDC.get("spanId"));
        if (result != null) {
            payload.put("resultType", result.getClass().getSimpleName());
        }
        AUDIT.info("{}", payload);
    }
}
