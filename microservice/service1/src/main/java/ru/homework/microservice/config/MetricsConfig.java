package ru.homework.microservice.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.repository.AccountRepository;
import ru.homework.microservice.repository.ClientRepository;

@Configuration
public class MetricsConfig {

    @Bean
    public Gauge clientsBlockedGauge(MeterRegistry registry, ClientRepository repo) {
        return Gauge.builder("clients.blocked.count", repo, r -> r.countByStatus(ClientStatus.BLACKLISTED))
                .description("Number of blacklisted clients")
                .register(registry);
    }

    @Bean
    public Gauge accountsArrestedGauge(MeterRegistry registry, AccountRepository repo) {
        return Gauge.builder("accounts.arrested.count", repo, r -> r.countByStatus(AccountStatus.ARRESTED))
                .description("Number of arrested accounts")
                .register(registry);
    }
}
