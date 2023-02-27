package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Override
    public UserDto create(User user) throws EmailAlreadyExistsException {
        if (existsByEmail(user.getEmail())) {
            String errorMessage = String.format("Пользователь с e-mail = '%s' уже зарегистрирован!", user.getEmail());
            log.warn(errorMessage);
            throw new EmailAlreadyExistsException(errorMessage);
        }

        return UserMapper.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto update(User user, Long userId) {
        if (!existsById(userId)) {
            String errorMessage = String.format("Пользовать с id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }

        if (existsByEmail(user.getEmail()) && !get(userId).getEmail().equals(user.getEmail())) {
            String errorMessage = String.format("E-mail '%s' занят другим пользователем!", user.getEmail());
            log.warn(errorMessage);
            throw new EmailAlreadyExistsException(errorMessage);
        }

        validate(user);

        return UserMapper.toUserDto(userRepository.update(user, userId));
    }

    @Override
    public UserDto get(Long userId) {
        if (!existsById(userId)) {
            String errorMessage = String.format("Пользовать с id = %d не найден!", userId);
            log.warn(errorMessage);
            throw new UserNotFoundException(errorMessage);
        }

        return UserMapper.toUserDto(userRepository.get(userId));
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long userId) {
        userRepository.delete(userId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.getEmailsList().contains(email);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.getIdsList().contains(id);
    }

    private void validate(User user) {
        if (user.getEmail() != null) {
            validator.validateProperty(user, "email");
        }
    }
}
