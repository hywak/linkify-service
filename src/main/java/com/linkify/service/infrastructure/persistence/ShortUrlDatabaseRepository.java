package com.linkify.service.infrastructure.persistence;

import com.linkify.service.domain.exception.UrlExpiredException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.domain.port.ShortUrlPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public class ShortUrlDatabaseRepository implements ShortUrlPersistence {
    Logger logger = LoggerFactory.getLogger(ShortUrlDatabaseRepository.class);
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    public ShortUrlDatabaseRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public void save(ShortUrlDomainModel shortUrlDomainModel) {
        String sql = "INSERT INTO short_url (slug, owner, original_url, expires_at) " +
                "VALUES (:slug, :owner, :originalUrl, :expiresAt)";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("slug", shortUrlDomainModel.getUrlSlug())
                .addValue("owner", shortUrlDomainModel.getOwner())
                .addValue("originalUrl", shortUrlDomainModel.getOriginalUrl())
                .addValue("expiresAt", shortUrlDomainModel.getExpirationDate());

        try {
            namedParameterJdbcTemplate.update(sql, parameters);
        } catch (DuplicateKeyException e) {
            logger.warn("Slug already exists in the database, trying to generate a new one");
            shortUrlDomainModel.regenerateSlug();

            parameters.addValue("slug", shortUrlDomainModel.getUrlSlug());
            namedParameterJdbcTemplate.update(sql, parameters);
        }
    }

    @Override
    public Optional<ShortUrlDomainModel> getShortUrl(String slug) {
        String sql = "SELECT * FROM short_url WHERE slug = :slug LIMIT 1";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("slug", slug);

        return namedParameterJdbcTemplate.query(sql, parameters, resultSet -> {
            if (resultSet.next()) {
                try {
                    ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(
                            resultSet.getString("original_url"),
                            resultSet.getString("owner"),
                            resultSet.getString("slug"),
                            resultSet.getObject("expires_at", OffsetDateTime.class)
                    );
                    return Optional.of(shortUrlDomainModel);
                } catch (UrlExpiredException e) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        });
    }

    @Override
    public Optional<ShortUrlDomainModel> getByOwnerAndOriginalUrl(String owner, String originalUrl) {
        String sql = "SELECT * FROM short_url WHERE owner = :owner AND original_url = :originalUrl " +
                "ORDER BY expires_at DESC NULLS FIRST LIMIT 1";

        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("owner", owner)
                .addValue("originalUrl", originalUrl);

        return namedParameterJdbcTemplate.query(sql, parameters, resultSet -> {
            if (resultSet.next()) {
                try {
                    ShortUrlDomainModel shortUrlDomainModel = new ShortUrlDomainModel(
                            resultSet.getString("original_url"),
                            resultSet.getString("owner"),
                            resultSet.getString("slug"),
                            resultSet.getObject("expires_at", OffsetDateTime.class)
                    );
                    return Optional.of(shortUrlDomainModel);
                } catch (UrlExpiredException e) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        });
    }
}
