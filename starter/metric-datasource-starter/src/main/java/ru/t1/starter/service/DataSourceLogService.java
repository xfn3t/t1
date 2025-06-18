package ru.t1.starter.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.starter.model.DataSourceErrorLog;
import ru.t1.starter.repository.DataSourceErrorLogRepository;

@Service
public class DataSourceLogService {

    private final DataSourceErrorLogRepository logRepo;

    public DataSourceLogService(DataSourceErrorLogRepository logRepo) {
        this.logRepo = logRepo;
    }

    /** ОТДЕЛЬНАЯ транзакция для записей лога */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(DataSourceErrorLog log) {
        logRepo.save(log);
    }
}
