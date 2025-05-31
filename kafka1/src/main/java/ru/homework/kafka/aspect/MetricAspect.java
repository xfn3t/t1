package ru.homework.kafka.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.homework.kafka.service.ErrorPublisher;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class MetricAspect {

    private final ErrorPublisher errorPublisher;

    @Value("${metrics.time-limit-ms}")
    private long timeLimitMs;

    @Pointcut("@annotation(ru.homework.kafka.annotation.Metric)")
    public void metricAnnotatedMethods() {}

    @Around("metricAnnotatedMethods()")
    public Object aroundMetric(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature msig = (MethodSignature) pjp.getSignature();
        String className = msig.getDeclaringType().getSimpleName();
        String methodName = msig.getName();

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        if (duration > timeLimitMs) {
            log.debug("Метод {}.{} выполнился {}ms (> {}ms)", className, methodName, duration, timeLimitMs);
            errorPublisher.publishMetricError(className, methodName, duration);
        } else {
            log.debug("Метод {}.{} выполнился {}ms (<= {}ms)", className, methodName, duration, timeLimitMs);
        }
        return result;
    }
}
