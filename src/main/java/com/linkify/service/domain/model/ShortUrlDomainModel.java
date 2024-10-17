package com.linkify.service.domain.model;

import com.linkify.service.domain.exception.InvalidUrlException;
import com.linkify.service.domain.exception.UrlExpiredException;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;


final public class ShortUrlDomainModel implements DomainModel {
    private final String originalUrl;
    private final String owner;
    private String slug;
    private final OffsetDateTime expirationDate;

    public ShortUrlDomainModel(String originalUrl, String owner, String slug, OffsetDateTime expirationDate) {
        isValidUrl(originalUrl);
        isExpired(expirationDate);

        this.originalUrl = originalUrl;
        this.slug = slug;
        this.owner = owner;
        this.expirationDate = expirationDate;
    }

    private void isValidUrl(String url) {
        if (url == null || url.isEmpty()) {
            throw new InvalidUrlException("URL cannot be empty");
        }

        try {
            URI uri = new URI(url);
            if (!uri.isAbsolute() || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme()))) {
                throw new InvalidUrlException("Invalid URL provided: Must start with http or https");
            }
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Invalid URL provided");
        }
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getUrlSlug() {
        if (slug == null) {
            slug = generateSlug();
        }

        return slug;
    }

    public void regenerateSlug() {
        slug = generateSlug();
    }

    private String generateSlug() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[8];
        random.nextBytes(bytes);
        String base64 = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        return base64.replace('+', '-').replace('/', '_');
    }

    public String getOwner() {
        return owner;
    }

    public OffsetDateTime getExpirationDate() {
        return expirationDate;
    }

    private void isExpired(OffsetDateTime expirationDate) {
        Optional<OffsetDateTime> expirationDateOptional = Optional.ofNullable(expirationDate);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (expirationDateOptional.isPresent() && expirationDateOptional.get().isBefore(now)) {
            throw new UrlExpiredException("URL has expired");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(slug);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortUrlDomainModel that = (ShortUrlDomainModel) o;
        return Objects.equals(slug, that.slug);
    }
}
