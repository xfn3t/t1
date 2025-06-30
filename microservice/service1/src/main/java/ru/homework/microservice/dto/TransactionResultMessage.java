package ru.homework.microservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.homework.microservice.common.TransactionStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResultMessage {
    private String transactionId;
    private String accountId;
    private TransactionStatus status;
}