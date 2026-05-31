package com.notification.email;

import com.notification.entity.UserNotificationOperation;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class EmailContentStrategyFactory {

    private final Map<UserNotificationOperation, EmailContentStrategy> strategies;

    public EmailContentStrategyFactory(List<EmailContentStrategy> strategyList) {
        Map<UserNotificationOperation, EmailContentStrategy> map = new EnumMap<>(UserNotificationOperation.class);
        for (EmailContentStrategy strategy : strategyList) {
            map.put(strategy.operation(), strategy);
        }
        this.strategies = Map.copyOf(map);
    }

    public EmailContentStrategy forOperation(UserNotificationOperation operation) {
        EmailContentStrategy strategy = strategies.get(operation);
        if (strategy == null) {
            throw new IllegalArgumentException("No email content strategy for operation: " + operation);
        }
        return strategy;
    }
}
