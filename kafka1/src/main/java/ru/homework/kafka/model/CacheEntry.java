package ru.homework.kafka.model;

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