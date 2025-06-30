package ru.t1.homework.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.homework.model.Client;
import ru.t1.homework.service.ClientService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<Client> createClient(@RequestBody Client client) {
        Client created = clientService.create(client);
        return ResponseEntity
                .created(URI.create("/api/clients/" + created.getClientId()))
                .body(created);
    }

    @GetMapping("/{clientUuid}")
    public Client getClient(@PathVariable UUID clientUuid) {
        return clientService.read(clientUuid);
    }

    @GetMapping
    public List<Client> listClients() {
        return clientService.readAll();
    }

    @PutMapping("/{clientUuid}")
    public Client updateClient(
            @PathVariable UUID clientUuid,
            @RequestBody Client client
    ) {
        return clientService.update(clientUuid, client);
    }

    @DeleteMapping("/{clientUuid}")
    public void deleteClient(@PathVariable UUID clientUuid) {
        clientService.delete(clientUuid);
    }
}