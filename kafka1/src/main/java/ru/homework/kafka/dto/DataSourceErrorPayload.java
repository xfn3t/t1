package ru.homework.kafka.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataSourceErrorPayload extends ErrorPayloadBase {

    private String exceptionMessage;

    public DataSourceErrorPayload(String className, String methodName, String exceptionMessage) {
        super(className, methodName);
        this.exceptionMessage = exceptionMessage;
    }
}