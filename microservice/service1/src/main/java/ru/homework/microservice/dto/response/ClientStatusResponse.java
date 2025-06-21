package ru.homework.microservice.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.homework.microservice.common.ClientStatus;

@Getter
@Setter
public class ClientStatusResponse {
    private String clientId;
    private ClientStatus status;
}