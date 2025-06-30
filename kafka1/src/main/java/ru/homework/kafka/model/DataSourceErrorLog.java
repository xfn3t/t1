package ru.homework.kafka.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "data_source_error_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataSourceErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_name", nullable = false)
    private String className;

    @Column(name = "method_name", nullable = false)
    private String methodName;

    @Column(name = "error_message", nullable = false, length = 1024)
    private String errorMessage;

    @Column(name = "logged_at", nullable = false)
    private LocalDateTime loggedAt;

    public DataSourceErrorLog(String className, String methodName, String errorMessage, LocalDateTime loggedAt) {
        this.className = className;
        this.methodName = methodName;
        this.errorMessage = errorMessage;
        this.loggedAt = loggedAt;
    }
}
