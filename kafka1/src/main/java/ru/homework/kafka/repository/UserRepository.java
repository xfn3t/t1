package ru.homework.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.homework.kafka.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
