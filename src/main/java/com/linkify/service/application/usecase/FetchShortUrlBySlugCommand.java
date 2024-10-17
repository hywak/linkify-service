package com.linkify.service.application.usecase;

public record FetchShortUrlBySlugCommand(String slug) implements UseCaseCommand {
}
