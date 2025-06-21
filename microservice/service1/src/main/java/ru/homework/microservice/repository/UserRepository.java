package ru.homework.microservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.homework.microservice.model.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
