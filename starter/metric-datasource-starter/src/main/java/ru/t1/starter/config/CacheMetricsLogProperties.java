package ru.t1.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache-metrics-log")
public class CacheMetricsLogProperties {

    private long defaultTtlSeconds;
    private long timeLimitMs;

    public long getDefaultTtlSeconds() { return defaultTtlSeconds; }
    public void setDefaultTtlSeconds(long defaultTtlSeconds) {
        this.defaultTtlSeconds = defaultTtlSeconds;
    }

    public long getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(long timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }
}
