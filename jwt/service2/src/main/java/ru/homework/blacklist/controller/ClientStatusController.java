package ru.homework.blacklist.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.homework.blacklist.common.ClientStatus;
import ru.homework.blacklist.dto.response.ClientStatusResponse;

import java.util.Random;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/clients")
public class ClientStatusController {

    private final Random rnd = new Random();

    @GetMapping("/{clientId}/status")
    public ClientStatusResponse status(@PathVariable UUID clientId) {
        log.info("Blacklist сервис: получили запрос статуса для clientId={}", clientId);
        boolean black = rnd.nextInt(100) < 10; // 10% chance
        ClientStatus st = black ? ClientStatus.BLACKLISTED : ClientStatus.OK;
        return new ClientStatusResponse(clientId, st);
    }
}