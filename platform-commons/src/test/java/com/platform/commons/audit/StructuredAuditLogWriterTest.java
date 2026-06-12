package com.platform.commons.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StructuredAuditLogWriterTest {

    @Test
    void writesAuditPayloadWithoutErrors() {
        AuditActorResolver actorResolver = () -> "bob";
        AuditMethodNameResolver methodNameResolver = joinPoint -> "SampleService.sample";
        StructuredAuditLogWriter writer = new StructuredAuditLogWriter(actorResolver, methodNameResolver);

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        AuditLog auditLog = mock(AuditLog.class);
        when(auditLog.action()).thenReturn("SAMPLE");
        when(auditLog.resourceType()).thenReturn("demo");

        assertThatCode(() -> writer.writeSuccess(auditLog, joinPoint, "done")).doesNotThrowAnyException();
        assertThatCode(() -> writer.writeSuccess(auditLog, joinPoint, null)).doesNotThrowAnyException();
    }
}
