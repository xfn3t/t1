package ru.t1.starter.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.t1.starter.model.DataSourceErrorLog;
import ru.t1.starter.service.DataSourceLogService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DataSourceErrorLoggingAspect {

    private final DataSourceLogService logService;

    public DataSourceErrorLoggingAspect(DataSourceLogService logService) {
        this.logService = logService;
    }

    @AfterThrowing(
            pointcut = "@within(ru.t1.starter.aop.LogDataSourceError)",
            throwing = "ex"
    )
    public void logAfterThrowing(JoinPoint jp, Throwable ex) {
        DataSourceErrorLog log = getDataSourceErrorLog(jp, ex);
        logService.saveLog(log);
    }

    private DataSourceErrorLog getDataSourceErrorLog(JoinPoint jp, Throwable ex) {

        DataSourceErrorLog errorLog = new DataSourceErrorLog();
        errorLog.setOccurredAt(Instant.now());
        errorLog.setMessage(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        errorLog.setStackTrace(getStackTraceString(ex));
        errorLog.setMethodSignature(jp.getSignature().toShortString());

        return errorLog;
    }

    private String getStackTraceString(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
