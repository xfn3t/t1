package ru.t1.starter.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "datasource_error_log")
public class DataSourceErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String stackTrace;

    private String message;

    private String methodSignature;

    private Instant occurredAt;

    public DataSourceErrorLog(Long id, String stackTrace, String message, String methodSignature, Instant occurredAt) {
        this.id = id;
        this.stackTrace = stackTrace;
        this.message = message;
        this.methodSignature = methodSignature;
        this.occurredAt = occurredAt;
    }

    public DataSourceErrorLog() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMethodSignature() {
        return methodSignature;
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(Instant occurredAt) {
        this.occurredAt = occurredAt;
    }
}
