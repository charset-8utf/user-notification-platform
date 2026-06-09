package com.notification.inbox;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationInboxRepository extends MongoRepository<NotificationInbox, String> {

    long countByStatus(InboxStatus status);

    List<NotificationInbox> findByStatusOrderByReceivedAtAsc(InboxStatus status, Pageable pageable);

    @Query("{ '_id': ?0, 'status': ?3 }")
    @Update("{ '$set': { 'status': ?1, 'processedAt': ?2 } }")
    long updateStatus(String eventId, InboxStatus newStatus, LocalDateTime processedAt, InboxStatus expectedStatus);
}
