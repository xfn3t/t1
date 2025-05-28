package ru.t1.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.homework.model.Account;
import ru.t1.homework.service.AccountService;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients/{clientId}/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @PathVariable UUID clientId,
            @RequestBody Account account
    ) {
        Account created = accountService.create(clientId, account);
        System.out.println(account.getType());
        System.out.println(clientId);
        return ResponseEntity
                .created(URI.create("/api/clients/" + clientId + "/accounts/" + created.getId()))
                .body(created);
    }

    @GetMapping("/{id}")
    public Account getAccount(
            @PathVariable UUID clientId,
            @PathVariable Long id
    ) {
        return accountService.read(id);
    }

    @GetMapping
    public List<Account> listAccounts(@PathVariable UUID clientId) {
        return accountService.readAll(clientId);
    }

    @PutMapping("/{id}")
    public Account updateAccount(
            @PathVariable Long id,
            @RequestBody Account account
    ) {
        return accountService.update(id, account);
    }

    @DeleteMapping("/{id}")
    public void deleteAccount(
            @PathVariable Long clientId,
            @PathVariable Long id
    ) {
        accountService.delete(id);
    }
}