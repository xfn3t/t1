package ru.t1.starter.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "time_limit_exceed_log")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public LocalDateTime getExceededAt() {
        return exceededAt;
    }

    public void setExceededAt(LocalDateTime exceededAt) {
        this.exceededAt = exceededAt;
    }
}