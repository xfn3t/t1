package ru.t1.starter.tester.service;

import org.springframework.stereotype.Service;
import ru.t1.starter.aop.Cached;
import ru.t1.starter.aop.LogDataSourceError;
import ru.t1.starter.aop.Metric;

@Service
@LogDataSourceError
public class DemoService {

    @Cached
    @Metric
    public String expensiveOperation(String input) throws InterruptedException {
        // Медленная операция
        Thread.sleep(300);
        return "Result for " + input + " at " + System.currentTimeMillis();
    }

    /** будет записано в datasource_error_log */
    public void alwaysFail() {
        throw new RuntimeException("Demo failure");
    }
}
