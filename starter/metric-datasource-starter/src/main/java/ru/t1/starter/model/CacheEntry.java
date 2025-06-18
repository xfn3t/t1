package ru.t1.starter.model;


public class CacheEntry {

    private final Object value;
    private final long expiryTimestamp;

    public Object getValue() {
        return value;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public CacheEntry(Object value, long expiryTimestamp) {
        this.value = value;
        this.expiryTimestamp = expiryTimestamp;
    }
}