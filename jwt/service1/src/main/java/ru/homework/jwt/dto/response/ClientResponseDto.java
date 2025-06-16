package ru.homework.jwt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ClientResponseDto {
    private Long id;
    private UUID clientId;
    private String firstName;
    private String middleName;
    private String lastName;
}