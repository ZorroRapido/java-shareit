package ru.practicum.shareit.request.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoTest {

    private final Validator validator;
    private final JacksonTester<ItemRequestDto> json;
    private ItemRequestDto itemRequestDto;

    public ItemRequestDtoTest(@Autowired JacksonTester<ItemRequestDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        itemRequestDto = new ItemRequestDto(1L, "description1", null, LocalDateTime.of(2022, 1, 1, 8, 0, 0), null);
    }

    @Test
    void testJsonItemRequestDto() throws Exception {
        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description1");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2022-01-01T08:00:00");
    }


    @Test
    void whenItemRequestDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenDescriptionIsBlankThenViolationsShouldBeReportedNotBlank() {
        itemRequestDto.setDescription(" ");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание запроса (description) не должно быть пустым!");
    }

    @Test
    void whenDescriptionIsNullThenViolationsShouldBeReportedNotBlank() {
        itemRequestDto.setDescription(null);
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание запроса (description) не должно быть пустым!");
    }

    @Test
    void whenDescriptionIsEmptyThenViolationsShouldBeReportedNotBlank() {
        itemRequestDto.setDescription("");
        Set<ConstraintViolation<ItemRequestDto>> violations = validator.validate(itemRequestDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание запроса (description) не должно быть пустым!");
    }
}
