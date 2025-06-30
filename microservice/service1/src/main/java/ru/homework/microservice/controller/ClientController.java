package ru.homework.microservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.dto.request.ClientRequestDto;
import ru.homework.microservice.dto.response.ClientResponseDto;
import ru.homework.microservice.dto.response.ClientStatusResponse;
import ru.homework.microservice.service.ClientService;
import ru.homework.microservice.service.clients.BlacklistClient;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final BlacklistClient blacklistClient;

    @GetMapping
    public ResponseEntity<List<ClientResponseDto>> listAll() {
        return ResponseEntity.ok(clientService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponseDto> create(@Valid @RequestBody ClientRequestDto dto) {
        return ResponseEntity.status(201).body(clientService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponseDto> update(@PathVariable Long id, @Valid @RequestBody ClientRequestDto dto) {
        return ResponseEntity.ok(clientService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ClientStatusResponse> getBlacklistStatus(@PathVariable Long id) {

        var dto = clientService.getById(id);
        String uuid = dto.getClientId().toString();

        var resp = blacklistClient.getStatus(uuid);

        return ResponseEntity.ok(resp);
    }

    @GetMapping("/metrics/blocked-count")
    public ResponseEntity<Long> getBlockedClientsCount() {
        long cnt = clientService.countByStatus(ClientStatus.BLACKLISTED);
        return ResponseEntity.ok(cnt);
    }
}