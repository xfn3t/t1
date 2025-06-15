package ru.homework.kafka.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.homework.kafka.dto.request.TransactionRequestDto;
import ru.homework.kafka.dto.response.TransactionResponseDto;
import ru.homework.kafka.model.transaction.Transaction;
import ru.homework.kafka.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService txService;

    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(@Valid @RequestBody TransactionRequestDto dto) {
        return ResponseEntity.status(201).body(txService.create(dto));
    }

    @GetMapping("/{id}")
    public TransactionResponseDto getById(@PathVariable String id) {
        return txService.getById(id);
    }

    @GetMapping
    public List<TransactionResponseDto> listByAccount(@RequestParam String accountId) {
        return txService.getByAccount(accountId);
    }
}