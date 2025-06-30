package ru.homework.blacklist.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.homework.blacklist.dto.response.UnlockResponse;
import ru.homework.blacklist.service.UnblockService;

import java.util.UUID;

@RestController
@RequestMapping("/api/unblock")
@AllArgsConstructor
public class UnblockController {

    private final UnblockService service;

    @PostMapping("/clients/{clientId}")
    public ResponseEntity<UnlockResponse> unblockClient(@PathVariable UUID clientId) {
        boolean success = service.unblockClient(clientId);
        return ResponseEntity.ok(new UnlockResponse(clientId, success));
    }

    @PostMapping("/accounts/{accountId}")
    public ResponseEntity<UnlockResponse> unblockAccount(@PathVariable String accountId) {
        boolean success = service.unblockAccount(accountId);
        return ResponseEntity.ok(new UnlockResponse(UUID.fromString("00000000-0000-0000-0000-000000000000"), success));
    }
}
