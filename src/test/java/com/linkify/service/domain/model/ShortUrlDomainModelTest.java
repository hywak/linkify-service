package com.linkify.service.domain.model;

import com.linkify.service.domain.exception.InvalidUrlException;
import com.linkify.service.domain.exception.UrlExpiredException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

final class ShortUrlDomainModelTest {

    @Test
    void testGetSlugGeneratesUniqueIdentifierWithTheLengthOf11Characters() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                null);

        // When
        String urlSlug = shortUrlDomainModel.getUrlSlug();

        // Then
        assertNotNull(urlSlug);
        assertEquals(11, urlSlug.length());
    }

    @Test
    void testGetSlugGeneratesUniqueIdentifierOnce() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                null);

        // When
        String urlSlug1 = shortUrlDomainModel.getUrlSlug();
        String urlSlug2 = shortUrlDomainModel.getUrlSlug();

        // Then
        assertEquals(urlSlug1, urlSlug2);
    }

    @Test
    void testGetSlugGeneratesUniqueIdentifierForDifferentInstances() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel1 = new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                null);
        ShortUrlDomainModel shortUrlDomainModel2 = new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                null);

        // When
        String urlSlug1 = shortUrlDomainModel1.getUrlSlug();
        String urlSlug2 = shortUrlDomainModel2.getUrlSlug();

        // Then
        assertNotEquals(urlSlug1, urlSlug2);
    }

    @Test
    void testItShouldCreateUrlDomainModel() {
        // Given
        String url = "https://www.google.com";
        String owner = "Michal";

        // When
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(url, owner, null, null);

        // Then
        assertEquals(url, shortUrlDomainModel.getOriginalUrl());
        assertEquals(owner, shortUrlDomainModel.getOwner());
    }

    @Test
    void testItShouldThrowInvalidUrlExceptionWhenUrlIsNull() {
        // Given
        String invalidUrl = null;

        // When
        Exception exception = assertThrows(InvalidUrlException.class, () -> new ShortUrlDomainModel(invalidUrl,
                "Michal", null, null));

        // Then
        assertEquals("URL cannot be empty", exception.getMessage());
    }

    @Test
    void testItShouldThrowInvalidUrlExceptionWhenUrlIsEmpty() {
        // Given
        String invalidUrl = "";

        // When
        Exception exception = assertThrows(InvalidUrlException.class, () -> new ShortUrlDomainModel(invalidUrl,
                "Michal", null, null));

        // Then
        assertEquals("URL cannot be empty", exception.getMessage());
    }

    @Test
    void testItShouldThrowInvalidUrlExceptionWhenUrlIsMissingProtocol() {
        // Given
        String invalidUrl = "invalid-url";

        // When
        Exception exception = assertThrows(InvalidUrlException.class, () -> new ShortUrlDomainModel(invalidUrl,
                "Michal", null, null));

        // Then
        assertEquals("Invalid URL provided: Must start with http or https", exception.getMessage());
    }

    @Test
    void testItShouldThrowInvalidUrlExceptionWhenUrlIsInvalid() {
        // Given
        String invalidUrl = "http://example.com/space here";

        // When
        Exception exception = assertThrows(InvalidUrlException.class, () -> new ShortUrlDomainModel(invalidUrl,
                "Michal", null, null));

        // Then
        assertEquals("Invalid URL provided", exception.getMessage());
    }

    @Test
    void testShouldReturnNotBeExpiredWhenExpirationDateNotProvided() {
        // Given & When & Then
        assertDoesNotThrow(() -> new ShortUrlDomainModel("https://www.google.com", "Michal", null, null));
    }

    @Test
    void testItShouldBeExpiredWhenExpirationDateIsAfterNow() {
        // Given & When & Then
        assertDoesNotThrow(() -> new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                OffsetDateTime.now().plusDays(1)));
    }

    @Test
    void testItShouldBeExpiredWhenExpirationDateIsBeforeNow() {
        // Given &  When
        Exception exception = assertThrows(UrlExpiredException.class, () -> new ShortUrlDomainModel("https://www" +
                ".google.com", "Michal", null, OffsetDateTime.now().minusDays(1)));

        // Then
        assertEquals("URL has expired", exception.getMessage());
    }

    @Test
    void testRegenerateSlugShouldGenerateNewSlug() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", null,
                null);
        String originalSlug = shortUrlDomainModel.getUrlSlug();

        // When
        shortUrlDomainModel.regenerateSlug();
        String newSlug = shortUrlDomainModel.getUrlSlug();

        // Then
        assertNotEquals(originalSlug, newSlug);
    }

    @Test
    void testHashCodeShouldBeConsistent() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("https://www.google.com", "Michal", "slug",
                null);

        // When
        int hashCode1 = shortUrlDomainModel.hashCode();
        int hashCode2 = shortUrlDomainModel.hashCode();

        // Then
        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testHashCodeShouldDifferForDifferentSlugs() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel1 = new ShortUrlDomainModel("https://www.google.com", "Michal", "slug1"
                , null);
        ShortUrlDomainModel shortUrlDomainModel2 = new ShortUrlDomainModel("https://www.google.com", "Michal", "slug2"
                , null);

        // When
        int hashCode1 = shortUrlDomainModel1.hashCode();
        int hashCode2 = shortUrlDomainModel2.hashCode();

        // Then
        assertNotEquals(hashCode1, hashCode2);
    }
}