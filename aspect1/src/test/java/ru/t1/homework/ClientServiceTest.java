package ru.t1.homework;

import ru.t1.homework.exception.DuplicateResourceException;
import ru.t1.homework.exception.ResourceNotFoundException;
import ru.t1.homework.model.Client;
import ru.t1.homework.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import ru.t1.homework.service.ClientService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    private Client sample;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        sample = Client.builder()
                .id(1L)
                .clientId(UUID.randomUUID())
                .firstName("Ivan")
                .middleName("Ivanovich")
                .lastName("Ivanov")
                .build();
    }

    @Test
    void create_success() {
        when(clientRepository.findByFirstNameAndMiddleNameAndLastName(
                sample.getFirstName(),
                sample.getMiddleName(),
                sample.getLastName())
        ).thenReturn(Optional.empty());

        when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
            Client c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        Client created = clientService.create(
                Client.builder()
                        .firstName(sample.getFirstName())
                        .middleName(sample.getMiddleName())
                        .lastName(sample.getLastName())
                        .build()
        );
        assertNotNull(created.getId());
        assertEquals(sample.getFirstName(), created.getFirstName());
        verify(clientRepository).save(any());
    }

    @Test
    void create_duplicate_throws() {
        when(clientRepository.findByFirstNameAndMiddleNameAndLastName(
                sample.getFirstName(), sample.getMiddleName(), sample.getLastName()))
                .thenReturn(Optional.of(sample));

        assertThrows(DuplicateResourceException.class, () ->
                clientService.create(sample)
        );
    }

    @Test
    void read_found() {
        when(clientRepository.findByClientId(sample.getClientId()))
                .thenReturn(Optional.of(sample));

        Client found = clientService.read(sample.getClientId());
        assertEquals(sample, found);
    }

    @Test
    void read_notFound_throws() {
        when(clientRepository.findByClientId(sample.getClientId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                clientService.read(sample.getClientId())
        );
    }

    @Test
    void readAll_returnsList() {
        when(clientRepository.findAll()).thenReturn(List.of(sample));
        List<Client> all = clientService.readAll();
        assertEquals(1, all.size());
        assertEquals(sample, all.get(0));
    }

    @Test
    void update_success() {
        when(clientRepository.findByClientId(sample.getClientId()))
                .thenReturn(Optional.of(sample));
        when(clientRepository.save(any())).thenReturn(sample);

        sample.setFirstName("Petr");
        Client updated = clientService.update(sample.getClientId(), sample);
        assertEquals("Petr", updated.getFirstName());
    }

    @Test
    void delete_success() {
        when(clientRepository.findByClientId(sample.getClientId()))
                .thenReturn(Optional.of(sample));
        doNothing().when(clientRepository).delete(sample);

        assertDoesNotThrow(() ->
                clientService.delete(sample.getClientId())
        );
        verify(clientRepository).delete(sample);
    }
}
