package ru.t1.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cache-metrics-log.kafka")
public class KafkaMetricsProperties {

    private boolean enabled;
    private String bootstrapServers;
    private String topic;

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getBootstrapServers() { return bootstrapServers; }
    public void setBootstrapServers(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}
