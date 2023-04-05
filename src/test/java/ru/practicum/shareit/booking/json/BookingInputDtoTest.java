package ru.practicum.shareit.booking.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingInputDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingInputDtoTest {

    private final JacksonTester<BookingInputDto> json;
    private BookingInputDto bookingInputDto;
    private final Validator validator;

    public BookingInputDtoTest(@Autowired JacksonTester<BookingInputDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        bookingInputDto = new BookingInputDto(
                1L,
                LocalDateTime.of(2030, 12, 25, 12, 0),
                LocalDateTime.of(2030, 12, 26, 12, 0));
    }

    @Test
    void testJsonBookingInputDto() throws Exception {
        JsonContent<BookingInputDto> result = json.write(bookingInputDto);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2030-12-25T12:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2030-12-26T12:00:00");
    }

    @Test
    void whenBookingInputDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isEmpty();
    }

    @Test
    void whenBookingInputDtoItemIdNotNullThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setItemId(null);
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указан id вещи (itemId)!");
    }

    @Test
    void whenBookingInputDtoStartNotNullThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setStart(null);
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указана дата и время начала бронирования (start)!");
    }

    @Test
    void whenBookingInputDtoEndNotNullThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setEnd(null);
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указана дата и время окончания бронирования (end)!");
    }

    @Test
    void whenBookingInputDtoStartBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setStart(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        System.out.println(violations);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Дата начала бронирования (start) должна содержать сегодняшнее " +
                "число или дату, которая ещё не наступила!");
    }

    @Test
    void whenBookingInputDtoEndBeforeNowThenViolationsShouldBeReportedNotNull() {
        bookingInputDto.setEnd(LocalDateTime.now().minusSeconds(1));
        Set<ConstraintViolation<BookingInputDto>> violations = validator.validate(bookingInputDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Дата окончания бронирования (end) должна содержать дату, которая " +
                "ещё не наступила!");
    }
}