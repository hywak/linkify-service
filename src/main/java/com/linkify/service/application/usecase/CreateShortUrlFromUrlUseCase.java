package com.linkify.service.application.usecase;

import com.linkify.service.domain.event.ShortLinkCreatedEvent;
import com.linkify.service.domain.exception.FailedToPersistException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import com.linkify.service.domain.port.ShortUrlPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CreateShortUrlFromUrlUseCase implements UseCase<CreateShortUrlFromUrlCommand, ShortUrlDomainModel> {
    Logger logger = LoggerFactory.getLogger(CreateShortUrlFromUrlUseCase.class);


    private final ShortUrlPersistence shortUrlCacheRepository;
    private final ShortUrlPersistence shortUrlDatabaseRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public CreateShortUrlFromUrlUseCase(ShortUrlPersistence shortUrlCacheRepository,
                                        ShortUrlPersistence shortUrlDatabaseRepository,
                                        ApplicationEventPublisher applicationEventPublisher
    ) {
        this.shortUrlCacheRepository = shortUrlCacheRepository;
        this.shortUrlDatabaseRepository = shortUrlDatabaseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public Optional<ShortUrlDomainModel> execute(CreateShortUrlFromUrlCommand command) {
        logger.debug("Trying to fetch short URL by owner {} and original URL: {} from cache", command.owner(),
                command.originalUrl());
        Optional<ShortUrlDomainModel> shortUrlDomainModel = shortUrlCacheRepository.getByOwnerAndOriginalUrl(
                command.owner(),
                command.originalUrl()
        );
        if (shortUrlDomainModel.isPresent()) {
            logger.debug("Short URL found in cache: {}", shortUrlDomainModel.get().getUrlSlug());
            return shortUrlDomainModel;
        }

        logger.debug("Short URL not found in cache, fetching from database");
        shortUrlDomainModel = shortUrlDatabaseRepository.getByOwnerAndOriginalUrl(
                command.owner(),
                command.originalUrl()
        );
        if (shortUrlDomainModel.isPresent()) {
            logger.debug("Short URL found in database: {}", shortUrlDomainModel.get().getUrlSlug());
            return shortUrlDomainModel;
        }

        ShortUrlDomainModel newShortUrlDomainModel = new ShortUrlDomainModel(
                command.originalUrl(), command.owner(), null, command.expirationDate()
        );

        logger.info("Creating new short URL for owner {} and original URL: {}", command.owner(), command.originalUrl());
        shortUrlDatabaseRepository.save(newShortUrlDomainModel);
        publishDomainEvent(newShortUrlDomainModel);

        try {
            logger.debug("Persisting new short URL to cache");
            shortUrlCacheRepository.save(newShortUrlDomainModel);
        } catch (FailedToPersistException e) {
            return Optional.empty();
        }


        return Optional.of(newShortUrlDomainModel);
    }

    private void publishDomainEvent(ShortUrlDomainModel shortUrlDomainModel) {
        applicationEventPublisher.publishEvent(
                ShortLinkCreatedEvent.createFromShortUrlDomainModel(
                        CreateShortUrlFromUrlUseCase.class,
                        shortUrlDomainModel
                )
        );
    }
}
