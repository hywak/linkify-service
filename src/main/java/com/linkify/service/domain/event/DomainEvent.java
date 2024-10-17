package com.linkify.service.domain.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;
import java.util.Optional;

public abstract class DomainEvent extends ApplicationEvent {
    public DomainEvent(Object source) {
        super(source);
    }

    public abstract String getEventName();

    public abstract Optional<Map<String, Object>> getEventPayload();
}
