package ru.homework.kafka.dto;

import lombok.Getter;
import lombok.Setter;
import ru.homework.kafka.common.TransactionStatus;

@Getter
@Setter
public class TransactionResultMessage {
    private String transactionId;
    private String accountId;
    private TransactionStatus status;
}