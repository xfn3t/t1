package ru.homework.jwt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.homework.jwt.model.user.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
