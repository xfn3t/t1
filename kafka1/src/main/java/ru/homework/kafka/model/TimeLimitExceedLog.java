package ru.homework.kafka.model;

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

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    public TimeLimitExceedLog(String className, String methodName, Long executionTimeMs, String errorMessage, LocalDateTime loggedAt) {
        this.className = className;
        this.methodName = methodName;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
        this.loggedAt = loggedAt;
    }
}
