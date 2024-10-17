package com.linkify.service.infrastructure.producer;

import com.linkify.service.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class DomainEventListener implements ApplicationListener<DomainEvent> {
    Logger logger = LoggerFactory.getLogger(DomainEventListener.class);

    @Override
    public void onApplicationEvent(DomainEvent event) {
        logger.info("Received domain event: {} with payload: {}. Publishing it to a queue...", event.getEventName(),
                event.getEventPayload());
    }
}
