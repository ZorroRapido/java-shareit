package ru.practicum.shareit.user.service;

import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    UserDto create(User user) throws EmailAlreadyExistsException;

    UserDto update(User user, Long userId);

    UserDto get(Long userId);

    List<UserDto> getAll();

    void delete(Long userId);

    boolean existsByEmail(String email);

    boolean existsById(Long id);
}
