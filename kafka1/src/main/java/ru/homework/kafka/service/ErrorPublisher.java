package ru.homework.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.homework.kafka.common.ErrorType;
import ru.homework.kafka.dto.DataSourceErrorPayload;
import ru.homework.kafka.dto.MetricErrorPayload;
import ru.homework.kafka.model.DataSourceErrorLog;
import ru.homework.kafka.model.TimeLimitExceedLog;
import ru.homework.kafka.repository.DataSourceErrorLogRepository;
import ru.homework.kafka.repository.TimeLimitExceedLogRepository;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ErrorPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final TimeLimitExceedLogRepository metricRepo;
    private final DataSourceErrorLogRepository dsRepo;

    private static final String TOPIC = "t1_demo_metrics";

    public void publishMetricError(String className, String methodName, Long durationMs) {
        MetricErrorPayload payloadDto = new MetricErrorPayload(className, methodName, durationMs);
        publish(
                ErrorType.METRICS,
                payloadDto,
                () -> {
                    TimeLimitExceedLog entry = new TimeLimitExceedLog(
                            className,
                            methodName,
                            durationMs,
                            "Kafka unreachable",
                            LocalDateTime.now()
                    );
                    metricRepo.save(entry);
                }
        );
    }

    public void publishDataSourceError(String className, String methodName, String exceptionMessage) {
        DataSourceErrorPayload payloadDto = new DataSourceErrorPayload(className, methodName, exceptionMessage);
        publish(
                ErrorType.DATA_SOURCE,
                payloadDto,
                () -> {
                    DataSourceErrorLog entry = new DataSourceErrorLog(
                            className,
                            methodName,
                            exceptionMessage,
                            LocalDateTime.now()
                    );
                    dsRepo.save(entry);
                }
        );
    }

    private <T> void publish(ErrorType errorType, T payloadDto, Runnable fallbackSaver) {
        final String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payloadDto);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации {}: {}", errorType, e.getMessage());
            fallbackSaver.run();
            return;
        }

        try {
            // Отправляем в Kafka; ждём до 3 секунд, чтобы бросилось исключение при недоступности
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(TOPIC, payloadJson);
            future.get(3, TimeUnit.SECONDS);
            log.info("Сообщение ({}) успешно отправлено: {}", errorType, payloadJson);
        } catch (Exception sendEx) {
            log.error("Не удалось отправить ({}) в Kafka: {}. Сохраняем в БД.", errorType, sendEx.getMessage());
            fallbackSaver.run();
        }
    }
}
