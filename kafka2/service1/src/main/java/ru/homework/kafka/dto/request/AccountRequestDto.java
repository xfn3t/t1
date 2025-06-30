package ru.homework.kafka.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
public class AccountRequestDto {
    @NotNull private Long clientId;
    @NotNull private BigDecimal initialBalance;
}
