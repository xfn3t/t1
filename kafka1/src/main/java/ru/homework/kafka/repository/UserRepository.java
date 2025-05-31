package ru.t1.homework.cache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.t1.homework.cache.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
