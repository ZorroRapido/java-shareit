package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    @Transactional(readOnly = true)
    boolean existsByEmail(String email);
}
