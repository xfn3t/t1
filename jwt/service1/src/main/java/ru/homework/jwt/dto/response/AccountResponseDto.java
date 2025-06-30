package ru.homework.jwt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.homework.jwt.common.AccountStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AccountResponseDto {
    private Long id;
    private UUID clientId;
    private String accountId;
    private AccountStatus status;
    private BigDecimal balance;
    private BigDecimal frozenAmount;
}