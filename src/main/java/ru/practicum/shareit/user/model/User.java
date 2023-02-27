package ru.practicum.shareit.user.model;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class User {
    private Long id;

    private String name;

    @NotNull(message = "Не указан адрес электронной почты (email)!")
    @Email(message = "Неверный формат поля email!")
    private String email;
}
