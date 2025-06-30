package ru.homework.jwt.dto.response;

import lombok.Getter;
import lombok.Setter;
import ru.homework.jwt.common.ClientStatus;

@Getter
@Setter
public class ClientStatusResponse {
    private String clientId;
    private ClientStatus status;
}