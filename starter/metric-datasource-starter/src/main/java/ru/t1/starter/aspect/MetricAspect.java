package ru.t1.starter.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.starter.model.TimeLimitExceedLog;
import ru.t1.starter.repository.TimeLimitExceedLogRepository;

import java.time.LocalDateTime;

@Aspect
@Component
public class MetricAspect {

    private final TimeLimitExceedLogRepository logRepository;
    private final long timeLimitMs;

    // Конструктор теперь принимает сразу и репозиторий, и порог
    public MetricAspect(TimeLimitExceedLogRepository logRepository, long timeLimitMs) {
        this.logRepository = logRepository;
        this.timeLimitMs = timeLimitMs;
    }

    @Pointcut("@annotation(ru.t1.starter.aop.Metric)")
    public void metricAnnotatedMethods() {}

    @Around("metricAnnotatedMethods()")
    public Object aroundMetric(ProceedingJoinPoint pjp) throws Throwable {
        String signature = pjp.getSignature().toShortString();

        long start = System.currentTimeMillis();
        Object result = pjp.proceed();
        long duration = System.currentTimeMillis() - start;

        if (duration > timeLimitMs) {

            MethodSignature ms = (MethodSignature) pjp.getSignature();
            String className = ms.getDeclaringType().getSimpleName();
            String methodName = ms.getName();

            TimeLimitExceedLog entry = new TimeLimitExceedLog(
                    className,
                    methodName,
                    duration,
                    LocalDateTime.now()
            );
            logRepository.save(entry);
        }
        return result;
    }
}
