package com.linkify.service.domain.event;

import com.linkify.service.domain.model.ShortUrlDomainModel;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ShortLinkFetchedEventTest {

    @Test
    void testCreateFromShortUrlDomainModelCreatesValidEvent() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);

        // When
        ShortLinkFetchedEvent event = ShortLinkFetchedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        assertEquals(ShortLinkFetchedEvent.eventName, event.getEventName());
        Optional<Map<String, Object>> payloadOptional = event.getEventPayload();
        assertTrue(payloadOptional.isPresent());

        Map<String, Object> payload = payloadOptional.get();
        assertEquals("https://www.google.com", payload.get("originalUrl"));
        assertEquals("Michal", payload.get("owner"));
        assertEquals(shortUrlDomainModel.getUrlSlug(), payload.get("slug"));
        assertNull(payload.get("expirationDate"));  // Expiration date was not provided, should be null
    }

    @Test
    void testCreateFromShortUrlDomainModelHandlesExpirationDate() {
        // Given
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(7);
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, expirationDate);

        // When
        ShortLinkFetchedEvent event = ShortLinkFetchedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        Optional<Map<String, Object>> payloadOptional = event.getEventPayload();
        assertTrue(payloadOptional.isPresent());

        Map<String, Object> payload = payloadOptional.get();
        assertEquals(expirationDate, payload.get("expirationDate")); // Expiration date should be present
    }

    @Test
    void testGetEventName() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);

        // When
        ShortLinkFetchedEvent event = ShortLinkFetchedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        assertEquals(ShortLinkFetchedEvent.eventName, event.getEventName());
    }

    @Test
    void testGetEventPayloadShouldReturnCorrectPayload() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null, null);

        // When
        ShortLinkFetchedEvent event = ShortLinkFetchedEvent.createFromShortUrlDomainModel(this, shortUrlDomainModel);

        // Then
        Optional<Map<String, Object>> payloadOptional = event.getEventPayload();
        assertTrue(payloadOptional.isPresent());
        Map<String, Object> payload = payloadOptional.get();

        assertEquals("https://www.google.com", payload.get("originalUrl"));
        assertEquals("Michal", payload.get("owner"));
        assertNotNull(payload.get("slug"));
    }
}
