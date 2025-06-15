package ru.homework.kafka.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionAcceptMessage {
    private UUID clientId;
    private String accountId;
    private String transactionId;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private BigDecimal balance;
}