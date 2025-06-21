package ru.homework.microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.dto.request.AccountRequestDto;
import ru.homework.microservice.dto.response.AccountResponseDto;
import ru.homework.microservice.service.AccountService;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> listAll() {
        return ResponseEntity.ok(accountService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(accountService.getById(id));
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> create(@Valid @RequestBody AccountRequestDto dto) {
        return ResponseEntity.status(201).body(accountService.create(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/metrics/arrested-count")
    public ResponseEntity<Long> getArrestedAccountsCount() {
        long cnt = accountService.countByStatus(AccountStatus.ARRESTED);
        return ResponseEntity.ok(cnt);
    }
}
