package com.notification.repository;

import com.notification.domain.InboxStatus;
import com.notification.entity.NotificationInbox;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
class NotificationInboxRepositoryImpl implements NotificationInboxRepositoryCustom {

    private static final String FIELD_STATUS = "status";
    private static final String FIELD_PROCESSING_STARTED_AT = "processingStartedAt";

    private final MongoTemplate mongoTemplate;

    @Override
    public List<NotificationInbox> claimPendingBatch(int batchSize) {
        List<NotificationInbox> claimed = new ArrayList<>(batchSize);
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < batchSize; i++) {
            Query query = new Query(Criteria.where(FIELD_STATUS).is(InboxStatus.PENDING))
                    .with(Sort.by(Sort.Direction.ASC, "receivedAt"));
            Update update = new Update()
                    .set(FIELD_STATUS, InboxStatus.PROCESSING)
                    .set(FIELD_PROCESSING_STARTED_AT, now);
            NotificationInbox row = mongoTemplate.findAndModify(
                    query,
                    update,
                    FindAndModifyOptions.options().returnNew(true),
                    NotificationInbox.class);
            if (row == null) {
                break;
            }
            claimed.add(row);
        }
        return claimed;
    }

    @Override
    public long requeueStaleProcessing(LocalDateTime staleBefore) {
        Query query = new Query(Criteria.where(FIELD_STATUS).is(InboxStatus.PROCESSING)
                .and(FIELD_PROCESSING_STARTED_AT).lt(staleBefore));
        Update update = new Update()
                .set(FIELD_STATUS, InboxStatus.PENDING)
                .unset(FIELD_PROCESSING_STARTED_AT);
        return mongoTemplate.updateMulti(query, update, NotificationInbox.class).getModifiedCount();
    }
}
