package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.List;

public interface UserRepository {
    User save(User user);

    User update(User user, Long userId);

    User get(Long userId);

    Collection<User> getAll();

    void delete(Long userId);

    List<String> getEmailsList();

    List<Long> getIdsList();
}
