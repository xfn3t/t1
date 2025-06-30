package ru.homework.blacklist.service;

import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class UnblockService {
    private final Random random = new Random();

    public boolean unblockClient(UUID clientId) {
        return random.nextBoolean();
    }

    public boolean unblockAccount(String accountId) {
        return random.nextInt(100) < 80;
    }
}