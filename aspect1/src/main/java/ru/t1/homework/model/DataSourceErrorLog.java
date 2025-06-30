package ru.t1.homework.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "datasource_error_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataSourceErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String stackTrace;

    private String message;

    private String methodSignature;

    private Instant occurredAt;
}
