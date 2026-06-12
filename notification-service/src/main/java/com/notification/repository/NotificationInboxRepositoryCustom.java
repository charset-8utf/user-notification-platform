package com.notification.repository;

import com.notification.entity.NotificationInbox;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationInboxRepositoryCustom {

    List<NotificationInbox> claimPendingBatch(int batchSize);

    long requeueStaleProcessing(LocalDateTime staleBefore);
}
