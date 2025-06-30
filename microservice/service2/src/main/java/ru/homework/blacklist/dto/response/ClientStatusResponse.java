package ru.homework.blacklist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.homework.blacklist.common.ClientStatus;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ClientStatusResponse {
    private UUID clientId;
    private ClientStatus status;
}