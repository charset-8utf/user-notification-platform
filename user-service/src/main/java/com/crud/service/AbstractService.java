package com.crud.service;

import com.crud.exception.DataAccessException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;

import java.util.function.Supplier;

/**
 * Базовый сервис с retry-механизмом.
 */
@Slf4j
public abstract class AbstractService {

    protected static final int MAX_RETRIES = 3;
    protected static final long RETRY_DELAY_MS = 100;

    protected <T> T executeWithRetry(Supplier<T> operation) {
        for (int attempt = 1; true; attempt++) {
            try {
                return operation.get();
            } catch (DataAccessException e) {
                boolean isOptimisticLock = e.getCause() instanceof StaleObjectStateException;
                boolean canRetry = attempt < MAX_RETRIES;
                if (isOptimisticLock && canRetry) {
                    log.warn("Конфликт версии, попытка {} из {}", attempt, MAX_RETRIES);
                    sleep();
                } else {
                    throw e;
                }
            }
        }
    }

    protected void sleep() {
        try {
            Thread.sleep(RETRY_DELAY_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataAccessException("Retry прерван", e);
        }
    }
}
