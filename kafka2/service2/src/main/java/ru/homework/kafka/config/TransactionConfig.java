package ru.homework.kafka.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "transaction.limit")
@Getter
@Setter
public class TransactionConfig {
    private int count;
    private int period;
}