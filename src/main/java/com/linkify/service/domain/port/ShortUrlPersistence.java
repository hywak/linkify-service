package com.linkify.service.domain.port;

import com.linkify.service.domain.model.ShortUrlDomainModel;

import java.util.Optional;

public interface ShortUrlPersistence {
    void save(ShortUrlDomainModel shortUrlDomainModel);

    Optional<ShortUrlDomainModel> getShortUrl(String slug);

    Optional<ShortUrlDomainModel> getByOwnerAndOriginalUrl(String owner, String originalUrl);
}
