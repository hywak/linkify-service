package com.linkify.service.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkify.service.domain.exception.FailedToFetchException;
import com.linkify.service.domain.exception.FailedToPersistException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.domain.port.ShortUrlPersistence;
import com.linkify.service.infrastructure.persistence.model.ShortUrlCacheModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class ShortUrlCacheRepository implements ShortUrlPersistence {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public ShortUrlCacheRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(ShortUrlDomainModel shortUrlDomainModel) {
        ShortUrlCacheModel shortUrlCacheModel = new ShortUrlCacheModel(shortUrlDomainModel.getOriginalUrl(),
                shortUrlDomainModel.getOwner(), shortUrlDomainModel.getExpirationDate());
        String json = toJson(shortUrlCacheModel);

        try {
            redisTemplate.opsForValue().set(shortUrlDomainModel.getUrlSlug(), json, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new FailedToPersistException("Failed to persist the URL to Redis", e);
        }
    }

    @Override
    public Optional<ShortUrlDomainModel> getShortUrl(String slug) {
        String json = redisTemplate.opsForValue().get(slug);
        if (json == null) {
            return Optional.empty();
        }

        ShortUrlCacheModel shortUrlCacheModel = fromJson(json);
        return Optional.of(
                new ShortUrlDomainModel(
                        shortUrlCacheModel.originalUrl(),
                        shortUrlCacheModel.owner(), slug,
                        shortUrlCacheModel.expirationDate()
                )
        );
    }

    @Override
    public Optional<ShortUrlDomainModel> getByOwnerAndOriginalUrl(String owner, String originalUrl) {
        return Optional.empty();
    }


    private String toJson(ShortUrlCacheModel shortUrlCacheModel) throws FailedToPersistException {
        try {
            return objectMapper.writeValueAsString(shortUrlCacheModel);
        } catch (Exception e) {
            throw new FailedToPersistException("Failed to persist the URL", e);
        }
    }

    private ShortUrlCacheModel fromJson(String json) throws FailedToFetchException {
        try {
            return objectMapper.readValue(json, ShortUrlCacheModel.class);
        } catch (JsonProcessingException e) {
            throw new FailedToFetchException("Error deserializing ShortUrlCacheModel from JSON", e);
        }
    }
}
