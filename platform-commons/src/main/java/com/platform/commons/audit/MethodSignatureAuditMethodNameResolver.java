package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
public class MethodSignatureAuditMethodNameResolver implements AuditMethodNameResolver {

    @Override
    public String resolve(ProceedingJoinPoint joinPoint) {
        if (joinPoint.getSignature() instanceof MethodSignature signature) {
            return signature.getDeclaringType().getSimpleName() + "." + signature.getName();
        }
        return joinPoint.getSignature().toShortString();
    }
}
