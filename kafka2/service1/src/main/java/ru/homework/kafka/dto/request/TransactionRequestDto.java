package ru.homework.kafka.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransactionRequestDto {
    @NotBlank private String accountId;
    @NotNull private BigDecimal amount;
}
