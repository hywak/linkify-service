package com.linkify.service.domain.event;

import com.linkify.service.domain.model.ShortUrlDomainModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShortLinkFetchedEvent extends DomainEvent {
    public static String eventName = "ShortLinkFetchedEvent";
    private final Map<String, Object> payload;

    private ShortLinkFetchedEvent(Object source, Map<String, Object> payload) {
        super(source);
        this.payload = payload;
    }

    public static ShortLinkFetchedEvent createFromShortUrlDomainModel(Object source,
                                                                      ShortUrlDomainModel shortUrlDomainModel) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("originalUrl", shortUrlDomainModel.getOriginalUrl());
        payload.put("slug", shortUrlDomainModel.getUrlSlug());
        payload.put("owner", shortUrlDomainModel.getOwner());

        if (shortUrlDomainModel.getExpirationDate() != null) {
            payload.put("expirationDate", shortUrlDomainModel.getExpirationDate());
        }
        return new ShortLinkFetchedEvent(source, payload);
    }

    @Override
    public String getEventName() {
        return eventName;
    }

    @Override
    public Optional<Map<String, Object>> getEventPayload() {
        return Optional.of(payload);
    }
}