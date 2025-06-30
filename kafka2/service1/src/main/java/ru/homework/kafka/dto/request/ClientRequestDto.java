package ru.homework.kafka.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRequestDto {
    @NotBlank
    private String firstName;
    @NotBlank
    private String middleName;
    @NotBlank
    private String lastName;
}
