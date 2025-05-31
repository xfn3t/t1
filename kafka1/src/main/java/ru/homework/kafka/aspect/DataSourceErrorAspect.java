package ru.homework.kafka.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import ru.homework.kafka.service.ErrorPublisher;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class DataSourceErrorAspect {

    private final ErrorPublisher errorPublisher;

    @Pointcut("@annotation(ru.t1.homework.datasource.annotation.LogDatasourceError)")
    public void datasourceAnnotatedMethods() {}

    @Around("datasourceAnnotatedMethods()")
    public Object aroundDatasource(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature msig = (MethodSignature) pjp.getSignature();
        String className = msig.getDeclaringType().getSimpleName();
        String methodName = msig.getName();

        try {
            return pjp.proceed();
        } catch (Exception ex) {
            log.error("Исключение в DATA SOURCE методе {}.{}: {}", className, methodName, ex.getMessage());
            errorPublisher.publishDataSourceError(className, methodName, ex.getMessage());
            throw ex;
        }
    }
}
