package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodSignatureAuditMethodNameResolverTest {

    private final MethodSignatureAuditMethodNameResolver resolver = new MethodSignatureAuditMethodNameResolver();

    @Test
    void resolvesMethodSignatureName() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getDeclaringType()).thenReturn(MethodSignatureAuditMethodNameResolver.class);
        when(signature.getName()).thenReturn("resolve");

        assertThat(resolver.resolve(joinPoint)).isEqualTo("MethodSignatureAuditMethodNameResolver.resolve");
    }

    @Test
    void fallsBackToShortStringForNonMethodSignature() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("MethodSignatureAuditMethodNameResolver.resolve(..)");

        assertThat(resolver.resolve(joinPoint)).isEqualTo("MethodSignatureAuditMethodNameResolver.resolve(..)");
    }
}
