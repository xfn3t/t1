package ru.t1.homework.cache.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_limit_exceed_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeLimitExceedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "execution_time_ms", nullable = false)
    private Long executionTimeMs;

    @Column(name = "exceeded_at", nullable = false)
    private LocalDateTime exceededAt;

    public TimeLimitExceedLog(String className, String methodName, Long executionTimeMs, LocalDateTime exceededAt) {
        this.className = className;
        this.methodName = methodName;
        this.executionTimeMs = executionTimeMs;
        this.exceededAt = exceededAt;
    }
}