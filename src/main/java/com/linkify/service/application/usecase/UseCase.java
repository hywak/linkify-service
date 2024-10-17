package com.linkify.service.application.usecase;


import java.util.Optional;

public interface UseCase<T, K> {
    Optional<K> execute(T command);
}
