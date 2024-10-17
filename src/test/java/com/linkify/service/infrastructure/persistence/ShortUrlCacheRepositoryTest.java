package com.linkify.service.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkify.service.domain.exception.FailedToFetchException;
import com.linkify.service.domain.exception.FailedToPersistException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.infrastructure.persistence.model.ShortUrlCacheModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ShortUrlCacheRepositoryTest {
    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ShortUrlCacheRepository shortUrlCacheRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testSaveSuccessfully() throws FailedToPersistException, JsonProcessingException {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("http://example.com", "owner", "slug", null);
        ShortUrlCacheModel shortUrlCacheModel = new ShortUrlCacheModel(shortUrlDomainModel.getOriginalUrl(),
                shortUrlDomainModel.getOwner(), shortUrlDomainModel.getExpirationDate());
        String json = "{\"originalUrl\":\"http://example.com\",\"owner\":\"owner\",\"expirationDate\":null}";

        // When
        when(objectMapper.writeValueAsString(shortUrlCacheModel)).thenReturn(
                json
        );
        when(redisTemplate.opsForValue()).thenReturn(Mockito.mock(ValueOperations.class));

        shortUrlCacheRepository.save(shortUrlDomainModel);

        // Then
        verify(redisTemplate.opsForValue()).set(eq("slug"), eq(json), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    public void testSaveThrowsFailedToPersistException() throws JsonProcessingException {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("http://example.com", "owner", "slug", null);
        ShortUrlCacheModel shortUrlCacheModel = new ShortUrlCacheModel(shortUrlDomainModel.getOriginalUrl(),
                shortUrlDomainModel.getOwner(), shortUrlDomainModel.getExpirationDate());
        String json = "{\"originalUrl\":\"http://example.com\",\"owner\":\"owner\",\"expirationDate\":null}";

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(shortUrlCacheModel)).thenReturn(json);

        doThrow(RuntimeException.class).when(valueOperations).set("slug", json, 24, TimeUnit.HOURS);

        // Then
        assertThrows(FailedToPersistException.class, () -> {
            shortUrlCacheRepository.save(shortUrlDomainModel);
        });

        verify(redisTemplate.opsForValue()).set(eq("slug"), eq(json), eq(24L), eq(TimeUnit.HOURS));
    }

    @Test
    public void testGetShortUrlThrowsFailedToFetchException() throws JsonProcessingException {
        // Given
        String json = "invalid json";

        ValueOperations<String, String> valueOperations = Mockito.mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("slug")).thenReturn(json);

        when(objectMapper.readValue(json, ShortUrlCacheModel.class)).thenThrow(JsonProcessingException.class);

        // Then
        assertThrows(FailedToFetchException.class, () -> {
            shortUrlCacheRepository.getShortUrl("slug");
        });

        verify(valueOperations).get("slug");
        verify(objectMapper).readValue(json, ShortUrlCacheModel.class);
    }

    @Test
    public void testGetShortUrlReturnsNull() throws JsonProcessingException, FailedToFetchException {
        // Given
        String json = null;

        // When
        when(redisTemplate.opsForValue()).thenReturn(Mockito.mock(ValueOperations.class));
        when(redisTemplate.opsForValue().get("slug")).thenReturn(json);

        Optional<ShortUrlDomainModel> result = shortUrlCacheRepository.getShortUrl("slug");

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetShortUrlSuccessfully() throws JsonProcessingException, FailedToFetchException {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("http://example.com", "owner", "slug", null);
        ShortUrlCacheModel shortUrlCacheModel = new ShortUrlCacheModel(shortUrlDomainModel.getOriginalUrl(),
                shortUrlDomainModel.getOwner(), shortUrlDomainModel.getExpirationDate());
        String json = "{\"originalUrl\":\"http://example.com\",\"owner\":\"owner\",\"expirationDate\":null}";

        // When
        when(redisTemplate.opsForValue()).thenReturn(Mockito.mock(ValueOperations.class));
        when(redisTemplate.opsForValue().get("slug")).thenReturn(json);
        when(objectMapper.readValue(json, ShortUrlCacheModel.class)).thenReturn(shortUrlCacheModel);

        Optional<ShortUrlDomainModel> result = shortUrlCacheRepository.getShortUrl("slug");

        // Then
        assertEquals(shortUrlDomainModel, result.get());
    }

    @Test
    public void testGetByOwnerAndOriginalUrlReturnsEmptyOptional() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";

        // When
        Optional<ShortUrlDomainModel> result = shortUrlCacheRepository.getByOwnerAndOriginalUrl(owner, originalUrl);

        // Then
        assertEquals(Optional.empty(), result);
    }

    @Test
    public void testToJsonThrowsFailedToPersistException() throws Exception {
        // Given
        ShortUrlCacheModel shortUrlCacheModel = new ShortUrlCacheModel("http://example.com", "owner", null);

        // Mock ObjectMapper to throw an exception when writeValueAsString is called
        when(objectMapper.writeValueAsString(shortUrlCacheModel)).thenThrow(new JsonProcessingException(
                "Serialization error") {});

        // Then
        FailedToPersistException exception = assertThrows(FailedToPersistException.class, () -> {
            shortUrlCacheRepository.save(new ShortUrlDomainModel("http://example.com", "owner", "slug", null));
        });

        // Verify the exception message
        assertEquals("Failed to persist the URL", exception.getMessage());
        assertInstanceOf(JsonProcessingException.class, exception.getCause());
    }
}

