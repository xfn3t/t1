package ru.homework.microservice.repository;

import feign.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.homework.microservice.common.ClientStatus;
import ru.homework.microservice.model.user.Client;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    long countByStatus(ClientStatus status);

    @Query("select c from Client c where c.status = :status")
    List<Client> findTopNByStatus(@Param("status") ClientStatus status, Pageable pageable);
}
