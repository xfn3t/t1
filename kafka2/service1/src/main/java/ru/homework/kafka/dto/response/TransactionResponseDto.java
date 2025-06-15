package ru.homework.kafka.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.homework.kafka.common.TransactionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class TransactionResponseDto {
    private UUID clientId;
    private String accountId;
    private String transactionId;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private TransactionStatus status;
}