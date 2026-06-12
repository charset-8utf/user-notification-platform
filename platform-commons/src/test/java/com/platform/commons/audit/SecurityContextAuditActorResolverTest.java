package com.platform.commons.audit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextAuditActorResolverTest {

    private final SecurityContextAuditActorResolver resolver = new SecurityContextAuditActorResolver();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsAuthenticatedPrincipalName() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("alice", "n/a", List.of()));

        assertThat(resolver.resolveActor()).isEqualTo("alice");
    }

    @Test
    void returnsAnonymousWhenNotAuthenticated() {
        assertThat(resolver.resolveActor()).isEqualTo("anonymous");
    }
}
