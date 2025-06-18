package ru.t1.starter.autoconfigure;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "metrics")
@Validated
public class MetricsProperties {

    @Min(0)
    private long timeLimitMs;

    public long getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(long timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }
}
