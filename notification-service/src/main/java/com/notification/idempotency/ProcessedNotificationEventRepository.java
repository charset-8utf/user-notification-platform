package com.notification.idempotency;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedNotificationEventRepository extends MongoRepository<ProcessedNotificationEvent, String> {
}
