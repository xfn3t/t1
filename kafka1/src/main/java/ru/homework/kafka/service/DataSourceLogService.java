package ru.homework.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.kafka.model.DataSourceErrorLog;
import ru.homework.kafka.repository.DataSourceErrorLogRepository;

@Service
@RequiredArgsConstructor
public class DataSourceLogService {

    private final DataSourceErrorLogRepository logRepo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(DataSourceErrorLog log) {
        logRepo.save(log);
    }
}
