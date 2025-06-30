package ru.t1.homework.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.t1.homework.aop.LogDataSourceError;
import ru.t1.homework.model.Client;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByFirstNameAndMiddleNameAndLastName(
            String firstName, String middleName, String lastName
    );
    Optional<Client> findByClientId(UUID clientId);

}