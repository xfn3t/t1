package ru.homework.kafka.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionMessage {
    private String accountId;
    private BigDecimal amount;
}