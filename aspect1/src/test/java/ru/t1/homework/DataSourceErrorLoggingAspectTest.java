package ru.t1.homework;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import org.springframework.jdbc.UncategorizedSQLException;
import ru.t1.homework.aop.LogDataSourceError;
import ru.t1.homework.aop.DataSourceErrorLoggingAspect;
import ru.t1.homework.model.DataSourceErrorLog;
import ru.t1.homework.repository.DataSourceErrorLogRepository;
import org.springframework.dao.*;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class DataSourceErrorLoggingAspectTest {
//
//    private DataSourceErrorLogRepository logRepo = mock(DataSourceErrorLogRepository.class);
//    private DataSourceErrorLoggingAspect aspect = new DataSourceErrorLoggingAspect(logRepo);
//
//    // Вспомогательный метод, создаёт прокси для любого Dummy-репозитория
//    private <T> T proxyFor(Class<T> iface, T impl) {
//        AspectJProxyFactory factory = new AspectJProxyFactory(impl);
//        factory.addAspect(aspect);
//        return factory.getProxy();
//    }
//
//    // Базовый интерфейс с аннотацией
//    interface DummyRepo {
//        @LogDataSourceError
//        void savePayload(String p) throws UncategorizedSQLException, DeadlockLoserDataAccessException;
//    }
//
//    /** 1. DataIntegrityViolationException */
//    static class IntegrityImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) {
//            throw new DataIntegrityViolationException("constraint violation");
//        }
//    }
//
//    /** 2. DuplicateKeyException */
//    static class DuplicateKeyImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) {
//            throw new DuplicateKeyException("duplicate key");
//        }
//    }
//
//    /** 3. DataAccessResourceFailureException */
//    static class ResourceFailureImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) {
//            throw new DataAccessResourceFailureException("resource down");
//        }
//    }
//
//    /** 4. DeadlockLoserDataAccessException */
//    static class DeadlockImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) throws DeadlockLoserDataAccessException {
//            throw new DeadlockLoserDataAccessException("deadlock", new SQLException("deadlock detected"));
//        }
//    }
//
//    /** 5. UncategorizedSQLException */
//    static class SqlExceptionImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) throws UncategorizedSQLException {
//            throw new UncategorizedSQLException("sql", "SELECT", new SQLException("db error"));
//        }
//    }
//
//    /** 6. Generic DataAccessException */
//    static class GenericDataAccessImpl implements DummyRepo {
//        @Override
//        @LogDataSourceError
//        public void savePayload(String p) {
//            throw new DataAccessException("generic failure") {};
//        }
//    }
//
//    @Nested
//    class WhenDatabaseExceptionsThrown {
//
//        @Test
//        void integrityViolation_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new IntegrityImpl());
//
//            assertThrows(DataIntegrityViolationException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().equals("constraint violation")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//
//        @Test
//        void duplicateKey_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new DuplicateKeyImpl());
//
//            assertThrows(DuplicateKeyException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().equals("duplicate key")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//
//        @Test
//        void resourceFailure_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new ResourceFailureImpl());
//
//            assertThrows(DataAccessResourceFailureException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().equals("resource down")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//
//        @Test
//        void deadlock_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new DeadlockImpl());
//
//            assertThrows(DeadlockLoserDataAccessException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().equals("deadlock")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//
//        @Test
//        void sqlException_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new SqlExceptionImpl());
//
//            assertThrows(UncategorizedSQLException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().contains("db error")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//
//        @Test
//        void genericDataAccess_logsAndRethrows() {
//            DummyRepo proxy = proxyFor(DummyRepo.class, new GenericDataAccessImpl());
//
//            assertThrows(DataAccessException.class, () -> proxy.savePayload("x"));
//
//            verify(logRepo, times(1)).save(argThat(log ->
//                    log.getMessage().equals("generic failure")
//                            && log.getMethodSignature().contains("savePayload")
//            ));
//        }
//    }
//
//    /** 7. Проверяем, что непредвиденные (не DataAccessException) исключения не логируются */
//    @Test
//    void nonDataAccessException_notLogged() {
//        DummyRepo proxy = proxyFor(DummyRepo.class, new DummyRepo() {
//            @Override
//            @LogDataSourceError
//            public void savePayload(String p) {
//                throw new IllegalStateException("not a DB error");
//            }
//        });
//
//        assertThrows(IllegalStateException.class, () -> proxy.savePayload("x"));
//        verify(logRepo, never()).save(any());
//    }
}
