package ru.t1.homework.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.homework.model.DataSourceErrorLog;
import ru.t1.homework.repository.DataSourceErrorLogRepository;

@Service
@RequiredArgsConstructor
public class DataSourceLogService {
    private final DataSourceErrorLogRepository logRepo;

    /** ОТДЕЛЬНАЯ транзакция для записей лога */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(DataSourceErrorLog log) {
        logRepo.save(log);
    }
}
