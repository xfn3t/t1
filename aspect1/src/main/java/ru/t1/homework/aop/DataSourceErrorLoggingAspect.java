package ru.t1.homework.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.homework.model.DataSourceErrorLog;
import ru.t1.homework.service.DataSourceLogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSourceErrorLoggingAspect {

    private final DataSourceLogService logService;

    @AfterThrowing(
            pointcut = "@within(ru.t1.homework.aop.LogDataSourceError)",
            throwing = "ex"
    )
    public void logAfterThrowing(JoinPoint jp, Throwable ex) {
        DataSourceErrorLog log = getDataSourceErrorLog(jp, ex);
        logService.saveLog(log);
    }

    private DataSourceErrorLog getDataSourceErrorLog(JoinPoint jp, Throwable ex) {
        return DataSourceErrorLog.builder()
                .occurredAt(Instant.now())
                .message(ex.getClass().getSimpleName() + ": " + ex.getMessage())
                .stackTrace(getStackTraceString(ex))
                .methodSignature(jp.getSignature().toShortString())
                .build();
    }

    private String getStackTraceString(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
