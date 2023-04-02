package ru.practicum.shareit.user.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.mapper.UserMapperImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Autowired
    private UserMapper userMapper;

    @Mock
    private UserRepository mockUserRepository;

    private UserService userService;
    private final User user = new User(1L, "user1", "first@user.ru");

    @BeforeEach
    void beforeEach() {
        userService = new UserServiceImpl(mockUserRepository, userMapper);
    }

    @Test
    void shouldExceptionWhenGetUserWithWrongId() {
        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(false);

        final UserNotFoundException exception = assertThrows(UserNotFoundException.class,
                () -> userService.get(-1L));
        assertEquals("Пользователь с id = -1 не найден!", exception.getMessage());
    }

    @Test
    void shouldExceptionWhenCreateUserWithExistEmail() {
        when(mockUserRepository.save(any()))
                .thenThrow(new DataIntegrityViolationException(""));

        final EmailAlreadyExistsException exp = assertThrows(EmailAlreadyExistsException.class,
                () -> userService.create(user));
        assertEquals(String.format("Пользователь с e-mail = '%s' уже зарегистрирован!", user.getEmail()),
                exp.getMessage());
    }

    @Test
    void shouldReturnUserWhenFindUserById() {
        when(mockUserRepository.existsById(any(Long.class)))
                .thenReturn(true);

        when(mockUserRepository.getReferenceById(any(Long.class)))
                .thenReturn(user);

        UserDto userDto = userService.get(1L);
        verify(mockUserRepository, Mockito.times(1))
                .getReferenceById(1L);

        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }
}