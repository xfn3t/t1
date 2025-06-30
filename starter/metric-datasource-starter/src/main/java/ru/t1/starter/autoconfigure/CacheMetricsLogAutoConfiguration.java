package ru.t1.starter.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import ru.t1.starter.aspect.CacheAspect;
import ru.t1.starter.aspect.DataSourceErrorLoggingAspect;
import ru.t1.starter.aspect.MetricAspect;
import ru.t1.starter.config.CacheMetricsLogProperties;
import ru.t1.starter.service.CacheService;
import ru.t1.starter.service.DataSourceLogService;
import ru.t1.starter.repository.*;

@AutoConfiguration
@AutoConfigureBefore(HibernateJpaAutoConfiguration.class)
@EnableConfigurationProperties(CacheMetricsLogProperties.class)
@EntityScan("ru.t1.starter.model")
@EnableJpaRepositories("ru.t1.starter.repository")
public class CacheMetricsLogAutoConfiguration {

    private final CacheMetricsLogProperties props;

    public CacheMetricsLogAutoConfiguration(CacheMetricsLogProperties props) {
        this.props = props;
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheService cacheService() {
        return new CacheService(props.getDefaultTtlSeconds());
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheAspect cacheAspect(CacheService cacheService) {
        return new CacheAspect(cacheService);
    }

    @Bean
    @ConditionalOnMissingBean
    public MetricAspect metricAspect(TimeLimitExceedLogRepository repo) {
        return new MetricAspect(repo, props.getTimeLimitMs());
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceLogService dataSourceLogService(DataSourceErrorLogRepository repo) {
        return new DataSourceLogService(repo);
    }

    @Bean
    @ConditionalOnMissingBean
    public DataSourceErrorLoggingAspect dataSourceErrorLoggingAspect(DataSourceLogService svc) {
        return new DataSourceErrorLoggingAspect(svc);
    }
}
