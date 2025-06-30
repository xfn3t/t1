package ru.homework.kafka.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.homework.kafka.model.CacheKey;
import ru.homework.kafka.service.CacheService;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheAspect {

    private final CacheService cacheService;

    @Pointcut("@annotation(ru.homework.kafka.annotation.Cached)")
    public void cachedMethods() {}

    @Around("cachedMethods()")
    public Object aroundCached(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String cls = sig.getDeclaringTypeName();
        String method = sig.getName();
        Object[] args = pjp.getArgs();
        CacheKey key = new CacheKey(cls, method, args);

        Object cached = cacheService.get(key);
        if (cached != null) {
            log.debug("Cache HIT {}", key);
            return cached;
        }
        log.debug("Cache MISS {}", key);
        Object result = pjp.proceed();
        if (result != null) {
            cacheService.put(key, result);
            log.debug("Cached {}", key);
        }
        return result;
    }
}