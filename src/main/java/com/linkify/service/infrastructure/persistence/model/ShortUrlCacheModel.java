package com.linkify.service.infrastructure.persistence.model;

import java.time.OffsetDateTime;

public record ShortUrlCacheModel(String originalUrl, String owner, OffsetDateTime expirationDate) {
}

