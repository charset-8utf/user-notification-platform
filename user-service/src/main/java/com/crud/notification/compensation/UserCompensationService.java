package com.crud.notification.compensation;

import com.crud.cache.UserCachePort;
import com.crud.entity.User;
import com.crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Компенсирующие транзакции для choreography saga (user-service).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserCompensationService {

    private final UserRepository userRepository;
    private final UserCachePort userCache;
    private final NotificationCompensationMetrics metrics;

    /**
     * Откатывает {@code USER_CREATED}: удаляет пользователя и сбрасывает кэш.
     */
    @Transactional
    public void rollbackUserCreate(User user, NotificationCompensationEvent event) {
        Long userId = user.getId();
        userRepository.delete(user);
        userCache.evict(userId);
        metrics.compensationApplied(event.originalOperation(), "ROLLBACK_DELETE");
        log.warn(
                "Компенсация saga: откат USER_CREATED — удалён userId={}, email={}, originalEventId={}, ошибка={}",
                userId,
                event.email(),
                event.originalEventId(),
                event.errorMessage());
    }

    /**
     * Фиксирует недоставку уведомления об удалении ({@code USER_DELETED}); откат удаления не выполняется.
     */
    public void signalDeleteNotificationUndelivered(NotificationCompensationEvent event) {
        metrics.compensationApplied(event.originalOperation(), "SIGNAL_ONLY");
        log.warn(
                "Компенсация saga: уведомление USER_DELETED не доставлено — email={}, originalEventId={}, ошибка={}",
                event.email(),
                event.originalEventId(),
                event.errorMessage());
    }
}
