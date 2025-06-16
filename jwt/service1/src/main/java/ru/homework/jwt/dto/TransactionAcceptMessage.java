package ru.homework.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionAcceptMessage {
    private UUID clientId;
    private String accountId;
    private String transactionId;
    private LocalDateTime timestamp;
    private BigDecimal amount;
    private BigDecimal balance;
}