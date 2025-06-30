package ru.t1.homework.cache.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.homework.cache.model.CacheKey;
import ru.t1.homework.cache.service.CacheService;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheAspect {

    private final CacheService cacheService;

    @Pointcut("@annotation(ru.t1.homework.cache.annotation.Cached)")
    public void cachedMethods() {}

    @Around("cachedMethods()")
    public Object aroundCached(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String classPackage = signature.getDeclaringType().getName();
        String className = classPackage.substring(classPackage.lastIndexOf('.') + 1);
        String methodName = signature.getName();
        Object[] args = pjp.getArgs();

        CacheKey key = new CacheKey(className, methodName, args);
        Object cachedValue = cacheService.get(key);
        if (cachedValue != null) {
            log.debug("[CacheAspect] Cache hit for key: {}. Returning cached value.", key);
            return cachedValue;
        }

        log.debug("[CacheAspect] Cache miss for key: {}. Proceeding to target method.", key);
        Object result = pjp.proceed();
        if (result != null) {
            cacheService.put(key, result);
            log.debug("[CacheAspect] Caching result for key: {}", key);
        } else {
            log.debug("[CacheAspect] Target method returned null for key: {}. Nothing cached.", key);
        }
        return result;
    }
}
