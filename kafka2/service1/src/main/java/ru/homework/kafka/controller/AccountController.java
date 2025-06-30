package ru.homework.kafka.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.homework.kafka.common.AccountStatus;
import ru.homework.kafka.dto.request.AccountRequestDto;
import ru.homework.kafka.dto.response.AccountResponseDto;
import ru.homework.kafka.service.AccountService;

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
}
