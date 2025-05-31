package ru.homework.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MetricErrorPayload extends ErrorPayloadBase {
    private Long durationMs;

    public MetricErrorPayload(String className, String methodName, Long durationMs) {
        super(className, methodName);
        this.durationMs = durationMs;
    }
}