package com.linkify.service.infrastructure.rest.url;

import com.linkify.service.application.usecase.CreateShortUrlFromUrlCommand;
import com.linkify.service.application.usecase.CreateShortUrlFromUrlUseCase;
import com.linkify.service.application.usecase.FetchShortUrlBySlugCommand;
import com.linkify.service.application.usecase.FetchShortUrlBySlugUseCase;
import com.linkify.service.domain.exception.FailedToFetchException;
import com.linkify.service.domain.exception.UrlNotFoundException;
import com.linkify.service.domain.model.ShortUrlDomainModel;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1/urls")
public class UrlShortenerController {
    private FetchShortUrlBySlugUseCase fetchShortUrlBySlugUseCase;
    private CreateShortUrlFromUrlUseCase createShortUrlFromUrlUseCase;

    public UrlShortenerController(FetchShortUrlBySlugUseCase fetchShortUrlBySlugUseCase,
                                  CreateShortUrlFromUrlUseCase createShortUrlFromUrlUseCase) {
        this.fetchShortUrlBySlugUseCase = fetchShortUrlBySlugUseCase;
        this.createShortUrlFromUrlUseCase = createShortUrlFromUrlUseCase;
    }

    @GetMapping("/{slug}")
    public ShortUrlResponse getShortUrl(@PathVariable String slug) {
        FetchShortUrlBySlugCommand command = new FetchShortUrlBySlugCommand(slug);
        try {
            Optional<ShortUrlDomainModel> shortUrlDomainModel = fetchShortUrlBySlugUseCase.execute(command);

            if (shortUrlDomainModel.isEmpty()) {
                throw new UrlNotFoundException("Failed to fetch short URL");
            }

            return new ShortUrlResponse(shortUrlDomainModel.get().getUrlSlug(),
                    shortUrlDomainModel.get().getOriginalUrl());
        } catch (FailedToFetchException e) {
            throw new UrlNotFoundException("Failed to fetch short URL");
        }
    }

    @PostMapping
    public ResponseEntity createShortUrl(@Valid @RequestBody ShortUrlRequest request) {
        CreateShortUrlFromUrlCommand command = new CreateShortUrlFromUrlCommand(request.originalUrl(),
                request.owner(), request.expirationDate().isEmpty() ? null : request.expirationDate().get());
        Optional<ShortUrlDomainModel> shortUrlDomainModel = createShortUrlFromUrlUseCase.execute(command);

        if (shortUrlDomainModel.isEmpty()) {
            throw new UrlNotFoundException("Failed to create short URL");
        }

        ShortUrlResponse response = new ShortUrlResponse(
                shortUrlDomainModel.get().getUrlSlug(),
                shortUrlDomainModel.get().getOriginalUrl()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
