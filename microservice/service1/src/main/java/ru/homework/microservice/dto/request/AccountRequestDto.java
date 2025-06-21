package ru.homework.microservice.dto.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {
    @NotNull private Long clientId;
    @NotNull private BigDecimal initialBalance;
}
