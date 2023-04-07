package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.common.service.ConsistencyService;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private final UserMapper userMapper;
    private final ConsistencyService consistencyService;

    @Transactional
    @Override
    public UserDto create(User user) throws EmailAlreadyExistsException {
        try {
            return userMapper.toUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) { // ?
            String errorMessage = String.format("Пользователь с e-mail = '%s' уже зарегистрирован!", user.getEmail());
            log.warn(errorMessage);
            throw new EmailAlreadyExistsException(errorMessage);
        }
    }

    @Transactional
    @Override
    public UserDto update(User user, Long userId) {
        consistencyService.checkUserExistence(userId);

        validateUser(user);

        if (userRepository.existsByEmail(user.getEmail()) && !get(userId).getEmail().equals(user.getEmail())) {
            String errorMessage = String.format("E-mail '%s' занят другим пользователем!", user.getEmail());
            log.warn(errorMessage);
            throw new EmailAlreadyExistsException(errorMessage);
        }

        User oldUser = userRepository.getReferenceById(userId);

        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }

        return userMapper.toUserDto(userRepository.save(oldUser));
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto get(Long userId) {
        consistencyService.checkUserExistence(userId);
        return userMapper.toUserDto(userRepository.getReferenceById(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void delete(Long userId) {
        userRepository.deleteById(userId);
    }

    private void validateUser(User user) {
        if (user.getEmail() != null) {
            Set<ConstraintViolation<User>> violations = validator.validateProperty(user, "email");
            if (violations.size() != 0) {
                throw new ValidationException(violations.iterator().next().getMessage());
            }
        }
    }
}
