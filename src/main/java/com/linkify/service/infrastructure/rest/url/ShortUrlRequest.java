package com.linkify.service.infrastructure.rest.url;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Optional;

public record ShortUrlRequest(
        @NotBlank(message = "Original URL cannot be empty")
        String originalUrl,

        @Size(max = 16, message = "Owner cannot exceed 16 characters")
        @NotBlank(message = "Owner cannot be empty")
        String owner,

        Optional<@Future(message = "Expiration date must be in the future") OffsetDateTime> expirationDate) {
}