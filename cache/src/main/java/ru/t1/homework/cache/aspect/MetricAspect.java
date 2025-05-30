package ru.t1.homework.cache.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.t1.homework.cache.model.TimeLimitExceedLog;
import ru.t1.homework.cache.repository.TimeLimitExceedLogRepository;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class MetricAspect {

    private final TimeLimitExceedLogRepository logRepository;

    @Value("${metrics.time-limit-ms}")
    private long timeLimitMs;

    @Pointcut("@annotation(ru.t1.homework.cache.annotation.Metric)")
    public void metricAnnotatedMethods() {}

    @Around("metricAnnotatedMethods()")
    public Object aroundMetric(ProceedingJoinPoint pjp) throws Throwable {

        String signature = pjp.getSignature().toShortString();
        log.debug("[MetricAspect] Intercepted method: {}. Threshold = {} ms", signature, timeLimitMs);

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        log.debug("[MetricAspect] Method {} executed in {} ms", signature, duration);

        if (duration > timeLimitMs) {
            log.info("[MetricAspect] Duration {} ms > threshold {} ms. Saving to DB...", duration, timeLimitMs);

            MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
            String className = methodSignature.getDeclaringType().getSimpleName();
            String methodName = methodSignature.getName();

            TimeLimitExceedLog logEntry = new TimeLimitExceedLog(
                    className,
                    methodName,
                    duration,
                    LocalDateTime.now()
            );
            logRepository.save(logEntry);
            log.info("[MetricAspect] Saved record to time_limit_exceed_log: {}.{} took {} ms",
                    className, methodName, duration);
        } else {
            log.debug("[MetricAspect] Duration {} ms <= threshold {} ms. Skipping save.", duration, timeLimitMs);
        }
        return result;
    }
}
