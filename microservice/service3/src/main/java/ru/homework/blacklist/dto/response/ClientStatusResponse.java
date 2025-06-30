package ru.homework.blacklist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.homework.blacklist.common.ClientStatus;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ClientStatusResponse {
    private UUID clientId;
    private ClientStatus status;
}