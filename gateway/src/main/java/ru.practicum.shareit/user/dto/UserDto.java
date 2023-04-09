package ru.practicum.shareit.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(message = "Не указано имя пользователя (name)!")
    private String name;

    @NotNull(message = "Не указан адрес электронной почты (email)!")
    @Email(message = "Неверный формат поля email!")
    private String email;
}
