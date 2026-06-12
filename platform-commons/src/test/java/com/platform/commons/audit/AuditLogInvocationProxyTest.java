package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuditLogInvocationProxyTest {

    @Test
    void invokesMethodAndWritesAudit() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AuditLog auditLog = mock(AuditLog.class);
        AuditLogWriter writer = mock(AuditLogWriter.class);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = AuditLogInvocationProxy.invokeWithAudit(joinPoint, auditLog, writer);

        assertThat(result).isEqualTo("ok");
        verify(joinPoint).proceed();
        verify(writer).writeSuccess(auditLog, joinPoint, "ok");
    }
}
