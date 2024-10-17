package com.linkify.service.application.usecase;

import java.time.OffsetDateTime;

public record CreateShortUrlFromUrlCommand(String originalUrl, String owner,
                                           OffsetDateTime expirationDate) implements UseCaseCommand {
}
