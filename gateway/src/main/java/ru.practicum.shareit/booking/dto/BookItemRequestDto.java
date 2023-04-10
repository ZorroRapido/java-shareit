package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {

    private long itemId;

    @NotNull(message = "Не указана дата и время начала бронирования (start)!")
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    @FutureOrPresent(message = "Дата начала бронирования (start) должна содержать сегодняшнее число или дату, которая " +
            "ещё не наступила!")
    private LocalDateTime start;

    @NotNull(message = "Не указана дата и время окончания бронирования (end)!")
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    @Future(message = "Дата окончания бронирования (end) должна содержать дату, которая ещё не наступила!")
    private LocalDateTime end;
}