package com.notification.repository;

import com.notification.entity.NotificationLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationLogRepository extends MongoRepository<NotificationLog, String> {

    Optional<NotificationLog> findFirstByEmailOrderByCreatedAtDesc(String email);
}
