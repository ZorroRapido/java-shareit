package ru.practicum.shareit.user.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.model.User;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserTest {

    private final Validator validator;
    private final JacksonTester<User> json;
    private User user;

    public UserTest(@Autowired JacksonTester<User> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        user = new User(1L, "user1", "first@user.ru");
    }

    @Test
    void testJsonUser() throws Exception {
        JsonContent<User> result = json.write(user);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("user1");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("first@user.ru");
    }

    @Test
    void whenUserIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenUserNameIsBlankThenViolationsShouldBeReportedNotBlank() {
        user.setName(" ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указано имя пользователя (name)!");
    }

    @Test
    void whenUserNameIsNullThenViolationsShouldBeReportedNotBlank() {
        user.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указано имя пользователя (name)!");
    }

    @Test
    void whenUserEmailIsBlankThenViolationsShouldBeReportedNotBlank() {
        user.setEmail(" ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Неверный формат поля email!");
    }

    @Test
    void whenUserEmailNotEmailThenViolationsShouldBeReportedNotEmail() {
        user.setEmail("user.user");
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Неверный формат поля email!");
    }

    @Test
    void whenUserEmailIsNullThenViolationsShouldBeReportedNotBlank() {
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указан адрес электронной почты (email)!");
    }
}