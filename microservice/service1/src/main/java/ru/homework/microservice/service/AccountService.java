package ru.homework.microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.microservice.common.AccountStatus;
import ru.homework.microservice.dto.request.AccountRequestDto;
import ru.homework.microservice.dto.response.AccountResponseDto;
import ru.homework.microservice.exception.UserNotFoundException;
import ru.homework.microservice.mapper.AccountMapper;
import ru.homework.microservice.model.user.Account;
import ru.homework.microservice.model.user.Client;
import ru.homework.microservice.repository.AccountRepository;
import ru.homework.microservice.repository.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final AccountMapper accountMapper;

    public List<AccountResponseDto> getAll() {
        return accountRepository.findAll().stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());
    }

    public AccountResponseDto getById(Long id) {
        Account acc = accountRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Account with id=" + id + " not found"));
        return accountMapper.toDto(acc);
    }

    @Transactional
    public AccountResponseDto create(AccountRequestDto dto) {
        Client client = clientRepository.findById(dto.getClientId())
                .orElseThrow(() -> new UserNotFoundException("Client with id=" + dto.getClientId() + " not found"));
        Account entity = accountMapper.toEntity(dto);
        entity.setClient(client);
        entity.setStatus(AccountStatus.OPEN);
        entity.setBalance(dto.getInitialBalance());
        Account saved = accountRepository.save(entity);
        return accountMapper.toDto(saved);
    }


    @Transactional
    public void delete(Long id) {
        if (!accountRepository.existsById(id)) {
            throw new UserNotFoundException("Account with id=" + id + " not found");
        }
        accountRepository.deleteById(id);
    }

    public long countByStatus(AccountStatus status) {
        return accountRepository.countByStatus(status);
    }
}
