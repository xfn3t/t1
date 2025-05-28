package ru.t1.homework;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        TransactionServiceTest.class,
        AccountServiceTest.class,
        ClientServiceTest.class,
        DataSourceErrorLoggingAspectTest.class
})
public class AllTestsSuite {}