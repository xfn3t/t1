package ru.homework.microservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.homework.microservice.common.ClientStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientStatusResponse {
    private String clientId;
    private ClientStatus status;
}