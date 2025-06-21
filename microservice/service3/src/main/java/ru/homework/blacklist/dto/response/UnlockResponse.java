package ru.homework.blacklist.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class UnlockResponse {
    private UUID id;
    private boolean unlocked;
}