package com.linkify.service.infrastructure.persistence;

import com.linkify.service.domain.model.ShortUrlDomainModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ShortUrlDatabaseRepositoryTest {

    @Mock
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @InjectMocks
    private ShortUrlDatabaseRepository shortUrlDatabaseRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testSaveSuccessfully() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("http://example.com", "owner", "slug",
                OffsetDateTime.now().plusDays(5));

        String sql = "INSERT INTO short_url (slug, owner, original_url, expires_at) " + "VALUES (:slug, :owner, " +
                ":originalUrl, :expiresAt)";

        // When
        shortUrlDatabaseRepository.save(shortUrlDomainModel);

        // Then
        verify(namedParameterJdbcTemplate).update(eq(sql), any(MapSqlParameterSource.class));
    }

    @Test
    public void testSaveRetriesOnDuplicateSlug() {
        // Given
        ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel("http://example.com", "owner", "slug",
                OffsetDateTime.now().plusDays(5));

        String sql = "INSERT INTO short_url (slug, owner, original_url, expires_at) " +
                "VALUES (:slug, :owner, :originalUrl, :expiresAt)";

        doThrow(new DuplicateKeyException("Duplicate slug"))
                .doReturn(1)
                .when(namedParameterJdbcTemplate).update(eq(sql), any(MapSqlParameterSource.class));

        // When
        shortUrlDatabaseRepository.save(shortUrlDomainModel);

        // Then
        verify(namedParameterJdbcTemplate, times(2)).update(eq(sql), any(MapSqlParameterSource.class));
    }

    @Test
    public void testGetShortUrlReturnsOptionalWithValue() {
        // Given
        String slug = "slug";
        ShortUrlDomainModel expectedShortUrl = new ShortUrlDomainModel("http://example.com", "owner", slug,
                OffsetDateTime.now().plusDays(5));

        when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(ResultSetExtractor.class))).thenReturn(Optional.of(expectedShortUrl));

        // When
        Optional<ShortUrlDomainModel> result = shortUrlDatabaseRepository.getShortUrl(slug);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedShortUrl, result.get());
    }

    @Test
    public void testGetShortUrlReturnsEmptyOptionalWhenNotFound() {
        // Given
        String slug = "non_existing_slug";

        when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(ResultSetExtractor.class))).thenReturn(Optional.empty());

        // When
        Optional<ShortUrlDomainModel> result = shortUrlDatabaseRepository.getShortUrl(slug);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetByOwnerAndOriginalUrlReturnsOptionalWithLongestExpiration() {
        // Given
        String owner = "owner";
        String originalUrl = "http://example.com";
        ShortUrlDomainModel shortUrlWithLongExpiration = new ShortUrlDomainModel(originalUrl, owner, "slug1",
                OffsetDateTime.now().plusDays(10));

        when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(ResultSetExtractor.class))).thenReturn(Optional.of(shortUrlWithLongExpiration));

        // When
        Optional<ShortUrlDomainModel> result = shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(owner, originalUrl);

        // Then
        assertTrue(result.isPresent());
        assertEquals(shortUrlWithLongExpiration, result.get());
    }

    @Test
    public void testGetByOwnerAndOriginalUrlReturnsEmptyWhenNotFound() {
        // Given
        String owner = "non_existing_owner";
        String originalUrl = "http://nonexistent.com";

        when(namedParameterJdbcTemplate.query(anyString(), any(MapSqlParameterSource.class),
                any(ResultSetExtractor.class))).thenReturn(Optional.empty());

        // When
        Optional<ShortUrlDomainModel> result = shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(owner, originalUrl);

        // Then
        assertFalse(result.isPresent());
    }
}
