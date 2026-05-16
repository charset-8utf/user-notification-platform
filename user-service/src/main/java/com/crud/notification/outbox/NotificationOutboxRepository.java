package com.crud.notification.outbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, Long> {

    List<NotificationOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationOutbox o
            SET o.status = :published, o.publishedAt = :now
            WHERE o.eventId = :eventId AND o.status = :pending
            """)
    int markPublished(
            @Param("eventId") UUID eventId,
            @Param("pending") OutboxStatus pending,
            @Param("published") OutboxStatus published,
            @Param("now") LocalDateTime now
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NotificationOutbox o
            SET o.status = :failed
            WHERE o.eventId = :eventId AND o.status = :pending
            """)
    int markFailed(
            @Param("eventId") UUID eventId,
            @Param("pending") OutboxStatus pending,
            @Param("failed") OutboxStatus failed
    );
}
