package ru.homework.jwt.dto;

import lombok.Getter;
import lombok.Setter;
import ru.homework.jwt.common.TransactionStatus;

@Getter
@Setter
public class TransactionResultMessage {
    private String transactionId;
    private String accountId;
    private TransactionStatus status;
}