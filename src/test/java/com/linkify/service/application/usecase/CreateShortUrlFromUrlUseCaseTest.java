package com.linkify.service.application.usecase;

import com.linkify.service.domain.event.ShortLinkCreatedEvent;
import com.linkify.service.domain.exception.FailedToPersistException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.domain.port.ShortUrlPersistence;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CreateShortUrlFromUrlUseCaseTest {

    private CreateShortUrlFromUrlUseCase createShortUrlFromUrlUseCase;

    @Mock
    private ShortUrlPersistence shortUrlCacheRepository;

    @Mock
    private ShortUrlPersistence shortUrlDatabaseRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        createShortUrlFromUrlUseCase = new CreateShortUrlFromUrlUseCase(shortUrlCacheRepository,
                shortUrlDatabaseRepository, applicationEventPublisher);
    }

    @Test
    public void testExecuteReturnsCachedShortUrl() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(5);
        ShortUrlDomainModel cachedShortUrl = new ShortUrlDomainModel(originalUrl, owner, "cachedSlug", expirationDate);

        when(shortUrlCacheRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.of(cachedShortUrl));

        // When
        Optional<ShortUrlDomainModel> result = createShortUrlFromUrlUseCase.execute(
                new CreateShortUrlFromUrlCommand(originalUrl, owner, expirationDate)
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(cachedShortUrl, result.get());
        verify(shortUrlCacheRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository, never()).getByOwnerAndOriginalUrl(any(), any());
        verify(shortUrlDatabaseRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(ShortLinkCreatedEvent.class);
    }

    @Test
    public void testExecuteReturnsShortUrlFromDatabase() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(5);
        ShortUrlDomainModel dbShortUrl = new ShortUrlDomainModel(originalUrl, owner, "dbSlug", expirationDate);

        when(shortUrlCacheRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.empty());
        when(shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.of(dbShortUrl));

        // When
        Optional<ShortUrlDomainModel> result = createShortUrlFromUrlUseCase.execute(
                new CreateShortUrlFromUrlCommand(originalUrl, owner, expirationDate)
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(dbShortUrl, result.get());
        verify(shortUrlCacheRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(ShortLinkCreatedEvent.class);
    }

    @Test
    public void testExecuteCreatesNewShortUrlWhenNotFoundInCacheOrDatabase() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(5);

        when(shortUrlCacheRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.empty());
        when(shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.empty());

        // When
        Optional<ShortUrlDomainModel> result = createShortUrlFromUrlUseCase.execute(
                new CreateShortUrlFromUrlCommand(originalUrl, owner, expirationDate)
        );

        // Then
        assertTrue(result.isPresent());
        assertEquals(originalUrl, result.get().getOriginalUrl());
        assertNotNull(result.get().getUrlSlug());

        verify(shortUrlCacheRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository).save(any(ShortUrlDomainModel.class));
        verify(shortUrlCacheRepository).save(any(ShortUrlDomainModel.class));
        verify(applicationEventPublisher).publishEvent(any(ShortLinkCreatedEvent.class));
    }

    @Test
    public void testExecuteHandlesFailedToPersistException() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";
        OffsetDateTime expirationDate = OffsetDateTime.now().plusDays(5);

        when(shortUrlCacheRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.empty());
        when(shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(owner, originalUrl))
                .thenReturn(Optional.empty());

        doThrow(new FailedToPersistException("Failed to save to cache", new Exception()))
                .when(shortUrlCacheRepository).save(any(ShortUrlDomainModel.class));

        // When
        Optional<ShortUrlDomainModel> result = createShortUrlFromUrlUseCase.execute(
                new CreateShortUrlFromUrlCommand(originalUrl, owner, expirationDate)
        );

        // Then
        assertTrue(result.isEmpty());

        verify(shortUrlCacheRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository).getByOwnerAndOriginalUrl(owner, originalUrl);
        verify(shortUrlDatabaseRepository).save(any(ShortUrlDomainModel.class));
        verify(shortUrlCacheRepository).save(any(ShortUrlDomainModel.class));
        verify(applicationEventPublisher).publishEvent(any(ShortLinkCreatedEvent.class));
    }
}
