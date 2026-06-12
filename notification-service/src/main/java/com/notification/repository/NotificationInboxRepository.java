package com.notification.repository;

import com.notification.domain.InboxStatus;
import com.notification.entity.NotificationInbox;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationInboxRepository extends MongoRepository<NotificationInbox, String>, NotificationInboxRepositoryCustom {

    long countByStatus(InboxStatus status);

    List<NotificationInbox> findByStatusOrderByReceivedAtAsc(InboxStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': ?3 }")
    @Update("{ '$set': { 'status': ?1, 'processedAt': ?2 } }")
    long updateStatus(
            String eventId,
            InboxStatus newStatus,
            @Nullable LocalDateTime processedAt,
            InboxStatus expectedStatus);
}
