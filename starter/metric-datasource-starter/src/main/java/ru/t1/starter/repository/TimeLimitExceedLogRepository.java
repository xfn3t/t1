package ru.t1.starter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.starter.model.TimeLimitExceedLog;

@Repository
public interface TimeLimitExceedLogRepository extends JpaRepository<TimeLimitExceedLog, Long> {
}
