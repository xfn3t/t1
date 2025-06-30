package ru.homework.jwt.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.jwt.dto.request.ClientRequestDto;
import ru.homework.jwt.dto.response.ClientResponseDto;
import ru.homework.jwt.exception.UserNotFoundException;
import ru.homework.jwt.mapper.ClientMapper;
import ru.homework.jwt.model.user.Client;
import ru.homework.jwt.repository.ClientRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public List<ClientResponseDto> getAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    public ClientResponseDto getById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Client with id=" + id + " not found"));
        return clientMapper.toDto(client);
    }

    @Transactional
    public ClientResponseDto create(ClientRequestDto dto) {
        Client entity = clientMapper.toEntity(dto);
        Client saved = clientRepository.save(entity);
        return clientMapper.toDto(saved);
    }

    @Transactional
    public ClientResponseDto update(Long id, ClientRequestDto dto) {
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Client with id=" + id + " not found"));
        existing.setFirstName(dto.getFirstName());
        existing.setMiddleName(dto.getMiddleName());
        existing.setLastName(dto.getLastName());
        return clientMapper.toDto(existing);
    }

    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new UserNotFoundException("Client with id=" + id + " not found");
        }
        clientRepository.deleteById(id);
    }
}
