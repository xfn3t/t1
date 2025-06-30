package ru.t1.homework.cache.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class CacheEntry {
    private final Object value;
    private final long expiryTimestamp;
}