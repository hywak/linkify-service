package com.linkify.service.application.usecase;

import com.linkify.service.domain.event.ShortLinkFetchedEvent;
import com.linkify.service.domain.exception.FailedToFetchException;
import com.linkify.service.domain.exception.UrlNotFoundException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.infrastructure.persistence.ShortUrlCacheRepository;
import com.linkify.service.infrastructure.persistence.ShortUrlDatabaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FetchShortUrlBySlugUseCase implements UseCase<FetchShortUrlBySlugCommand, ShortUrlDomainModel> {
    Logger logger = LoggerFactory.getLogger(FetchShortUrlBySlugUseCase.class);

    private final ShortUrlCacheRepository shortUrlCacheRepository;
    private final ShortUrlDatabaseRepository shortUrlDatabaseRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public FetchShortUrlBySlugUseCase(
            ShortUrlCacheRepository shortUrlCacheRepository,
            ShortUrlDatabaseRepository shortUrlDatabaseRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.shortUrlCacheRepository = shortUrlCacheRepository;
        this.shortUrlDatabaseRepository = shortUrlDatabaseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<ShortUrlDomainModel> execute(FetchShortUrlBySlugCommand command) throws FailedToFetchException {
        logger.info("Fetching short URL by slug: {}", command.slug());
        Optional<ShortUrlDomainModel> shortUrlDomainModel = shortUrlCacheRepository.getShortUrl(command.slug());
        logger.debug("Short URL found in cache: {}", shortUrlDomainModel.isPresent());
        if (shortUrlDomainModel.isEmpty()) {
            logger.debug("Short URL not found in cache, fetching from database");
            shortUrlDomainModel = shortUrlDatabaseRepository.getShortUrl(command.slug());
        }

        if (shortUrlDomainModel.isEmpty()) {
            logger.warn("Short URL not found for slug: {}", command.slug());
            throw new UrlNotFoundException("URL not found for slug: " + command.slug());
        }

        logger.info("Short URL found for slug: {}", command.slug());
        publishDomainEvent(shortUrlDomainModel.get());
        return shortUrlDomainModel;
    }


    private void publishDomainEvent(ShortUrlDomainModel shortUrlDomainModel) {
        applicationEventPublisher.publishEvent(
                ShortLinkFetchedEvent.createFromShortUrlDomainModel(
                        FetchShortUrlBySlugUseCase.class,
                        shortUrlDomainModel
                )
        );
    }
}
