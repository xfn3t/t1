package ru.homework.microservice.dto;

import lombok.Getter;
import lombok.Setter;
import ru.homework.microservice.common.TransactionStatus;

@Getter
@Setter
public class TransactionResultMessage {
    private String transactionId;
    private String accountId;
    private TransactionStatus status;
}