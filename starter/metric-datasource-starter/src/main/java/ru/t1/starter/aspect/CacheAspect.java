package ru.t1.starter.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.t1.starter.model.CacheKey;
import ru.t1.starter.service.CacheService;

@Aspect
@Component
public class CacheAspect {

    private final CacheService cacheService;

    public CacheAspect(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Pointcut("@annotation(ru.t1.starter.aop.Cached)")
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
            return cachedValue;
        }

        Object result = pjp.proceed();
        if (result != null) {
            cacheService.put(key, result);
        }
        return result;
    }
}
