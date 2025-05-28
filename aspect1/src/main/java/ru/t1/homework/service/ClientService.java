package ru.t1.homework.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.t1.homework.aop.LogDataSourceError;
import ru.t1.homework.exception.DuplicateResourceException;
import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Client;
import ru.t1.homework.repository.ClientRepository;

import java.util.List;
import java.util.UUID;

@Service
@LogDataSourceError
@AllArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.SERIALIZABLE
    )
    public Client create(Client client) {
        clientRepository.findByFirstNameAndMiddleNameAndLastName(
                client.getFirstName(),
                client.getMiddleName(),
                client.getLastName()
        ).ifPresent(existing -> {
            throw new DuplicateResourceException("Client already exists");
        });
        return clientRepository.save(client);
    }

    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public Client read(UUID clientUuid) {
        return clientRepository.findByClientId(clientUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public List<Client> readAll() {
        return clientRepository.findAll();
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.READ_COMMITTED
    )
    public Client update(UUID clientUuid, Client client) {
        Client existing = read(clientUuid);
        existing.setFirstName(client.getFirstName());
        existing.setMiddleName(client.getMiddleName());
        existing.setLastName(client.getLastName());
        return clientRepository.save(existing);
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.READ_COMMITTED
    )
    public void delete(UUID clientUuid) {
        Client existing = read(clientUuid);
        clientRepository.delete(existing);
        clientRepository.flush();
    }

    @Transactional(
            propagation = Propagation.SUPPORTS,
            readOnly    = true
    )
    public Client read(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found"));
    }

    @Transactional(
            propagation = Propagation.REQUIRED,
            isolation   = Isolation.READ_COMMITTED
    )
    public Client update(Long clientId, Client client) {
        Client existing = read(clientId);
        existing.setFirstName(client.getFirstName());
        existing.setMiddleName(client.getMiddleName());
        existing.setLastName(client.getLastName());
        return clientRepository.save(existing);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void delete(Long clientId) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client not found");
        }
        clientRepository.deleteById(clientId);
        clientRepository.flush();
    }
}
