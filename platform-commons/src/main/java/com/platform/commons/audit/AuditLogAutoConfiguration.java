package com.platform.commons.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnClass(name = "org.springframework.security.core.context.SecurityContextHolder")
@Import({
        AuditLogAspect.class,
        SecurityContextAuditActorResolver.class,
        MethodSignatureAuditMethodNameResolver.class,
        StructuredAuditLogWriter.class
})
public class AuditLogAutoConfiguration {
}
