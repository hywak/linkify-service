package com.linkify.service.domain.event;

import com.linkify.service.domain.model.ShortUrlDomainModel;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShortLinkCreatedEventTest {

    @Test
    void testCreateFromShortUrlDomainModelCreatesValidEvent() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);

        // When
        ShortLinkCreatedEvent event = ShortLinkCreatedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        assertNotNull(event);
        assertEquals(ShortLinkCreatedEvent.eventName, event.getEventName());

        Optional<Map<String, Object>> payload = event.getEventPayload();
        assertTrue(payload.isPresent());
        Map<String, Object> payloadData = payload.get();
        assertEquals(shortUrlDomainModel.getOriginalUrl(), payloadData.get("originalUrl"));
        assertEquals(shortUrlDomainModel.getUrlSlug(), payloadData.get("slug"));
        assertEquals(shortUrlDomainModel.getOwner(), payloadData.get("owner"));
        assertNull(payloadData.get("expirationDate"));
    }

    @Test
    void testCreateFromShortUrlDomainModelIncludesExpirationDateIfPresent() {
        // Given
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(7);
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, expirationDate);

        // When
        ShortLinkCreatedEvent event = ShortLinkCreatedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        assertNotNull(event);
        assertEquals(ShortLinkCreatedEvent.eventName, event.getEventName());

        Optional<Map<String, Object>> payload = event.getEventPayload();
        assertTrue(payload.isPresent());
        Map<String, Object> payloadData = payload.get();
        assertEquals(expirationDate, payloadData.get("expirationDate"));
    }

    @Test
    void testGetEventNameReturnsCorrectEventName() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);
        ShortLinkCreatedEvent event = ShortLinkCreatedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // When
        String eventName = event.getEventName();

        // Then
        assertEquals(ShortLinkCreatedEvent.eventName, eventName);
    }

    @Test
    void testGetEventPayloadReturnsCorrectPayload() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);

        // When
        ShortLinkCreatedEvent event = ShortLinkCreatedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);
        Optional<Map<String, Object>> eventPayload = event.getEventPayload();

        // Then
        assertTrue(eventPayload.isPresent());
        Map<String, Object> payload = eventPayload.get();
        assertEquals("https://www.google.com", payload.get("originalUrl"));
        assertEquals(shortUrlDomainModel.getUrlSlug(), payload.get("slug"));
        assertEquals("Michal", payload.get("owner"));
        assertNull(payload.get("expirationDate"));
    }
}
