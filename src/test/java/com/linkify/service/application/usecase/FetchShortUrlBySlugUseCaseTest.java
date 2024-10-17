package com.linkify.service.application.usecase;

import com.linkify.service.domain.event.ShortLinkFetchedEvent;
import com.linkify.service.domain.exception.FailedToFetchException;
import com.linkify.service.domain.exception.UrlNotFoundException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.infrastructure.persistence.ShortUrlCacheRepository;
import com.linkify.service.infrastructure.persistence.ShortUrlDatabaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FetchShortUrlBySlugUseCaseTest {

    @Mock
    private ShortUrlCacheRepository shortUrlCacheRepository;

    @Mock
    private ShortUrlDatabaseRepository shortUrlDatabaseRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private FetchShortUrlBySlugUseCase fetchShortUrlBySlugUseCase;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExecuteFetchesFromCache() throws FailedToFetchException {
        // Given
        String slug = "slug";
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(
                "http://example.com", "owner", slug, OffsetDateTime.now().plusDays(5)
        );

        when(shortUrlCacheRepository.getShortUrl(slug)).thenReturn(Optional.of(shortUrlDomainModel));

        // When
        Optional<ShortUrlDomainModel> result = fetchShortUrlBySlugUseCase.execute(new FetchShortUrlBySlugCommand(slug));

        // Then
        assertTrue(result.isPresent());
        assertEquals(shortUrlDomainModel, result.get());
        verify(shortUrlCacheRepository).getShortUrl(slug);
        verifyNoInteractions(shortUrlDatabaseRepository);
        verify(applicationEventPublisher).publishEvent(any(ShortLinkFetchedEvent.class));
    }

    @Test
    public void testExecuteFetchesFromDatabaseWhenNotInCache() throws FailedToFetchException {
        // Given
        String slug = "slug";
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(
                "http://example.com", "owner", slug, OffsetDateTime.now().plusDays(5)
        );

        when(shortUrlCacheRepository.getShortUrl(slug)).thenReturn(Optional.empty());
        when(shortUrlDatabaseRepository.getShortUrl(slug)).thenReturn(Optional.of(shortUrlDomainModel));

        // When
        Optional<ShortUrlDomainModel> result = fetchShortUrlBySlugUseCase.execute(new FetchShortUrlBySlugCommand(slug));

        // Then
        assertTrue(result.isPresent());
        assertEquals(shortUrlDomainModel, result.get());
        verify(shortUrlCacheRepository).getShortUrl(slug);
        verify(shortUrlDatabaseRepository).getShortUrl(slug);
        verify(applicationEventPublisher).publishEvent(any(ShortLinkFetchedEvent.class));
    }

    @Test
    public void testExecuteThrowsUrlNotFoundExceptionWhenNotInCacheOrDatabase() {
        // Given
        String slug = "non_existing_slug";

        when(shortUrlCacheRepository.getShortUrl(slug)).thenReturn(Optional.empty());
        when(shortUrlDatabaseRepository.getShortUrl(slug)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UrlNotFoundException.class, () -> {
            fetchShortUrlBySlugUseCase.execute(new FetchShortUrlBySlugCommand(slug));
        });

        verify(shortUrlCacheRepository).getShortUrl(slug);
        verify(shortUrlDatabaseRepository).getShortUrl(slug);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    public void testExecutePublishesEventWhenUrlIsFound() throws FailedToFetchException {
        // Given
        String slug = "slug";
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(
                "http://example.com", "owner", slug, OffsetDateTime.now().plusDays(5)
        );

        when(shortUrlCacheRepository.getShortUrl(slug)).thenReturn(Optional.of(shortUrlDomainModel));

        // When
        fetchShortUrlBySlugUseCase.execute(new FetchShortUrlBySlugCommand(slug));

        // Then
        verify(applicationEventPublisher).publishEvent(any(ShortLinkFetchedEvent.class));
    }
}
