package ru.practicum.shareit.item.json;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoTest {

    private final Validator validator;
    private final JacksonTester<ItemDto> json;
    private ItemDto itemDto;

    public ItemDtoTest(@Autowired JacksonTester<ItemDto> json) {
        this.json = json;
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @BeforeEach
    void beforeEach() {
        itemDto = new ItemDto(1L, "item1", "description1", true, null, null);
    }

    @Test
    void testJsonItemDto() throws Exception {
        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("item1");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("description1");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(true);
    }


    @Test
    void whenItemDtoIsValidThenViolationsShouldBeEmpty() {
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isEmpty();
    }

    @Test
    void whenNameIsBlankThenViolationsShouldBeReportedNotBlank() {
        itemDto.setName(" ");
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Название (name) вещи не должно быть пустым!");
    }

    @Test
    void whenNameIsNullThenViolationsShouldBeReportedNotBlank() {
        itemDto.setName(null);
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Название (name) вещи не должно быть пустым!'");
    }

    @Test
    void whenNameIsEmptyThenViolationsShouldBeReportedNotBlank() {
        itemDto.setName("");
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Название (name) вещи не должно быть пустым!'");
    }

    @Test
    void whenDescriptionIsBlankThenViolationsShouldBeReportedNotBlank() {
        itemDto.setDescription(" ");
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание (description) вещи не должно быть пустым!");
    }

    @Test
    void whenDescriptionIsNullThenViolationsShouldBeReportedNotBlank() {
        itemDto.setDescription(null);
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание (description) вещи не должно быть пустым!");
    }

    @Test
    void whenDescriptionIsEmptyThenViolationsShouldBeReportedNotBlank() {
        itemDto.setDescription("");
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Описание (description) вещи не должно быть пустым!");
    }

    @Test
    void whenAvailableIsNullThenViolationsShouldBeReportedNotNull() {
        itemDto.setAvailable(null);
        Set<ConstraintViolation<ItemDto>> violations = validator.validate(itemDto);

        assertThat(violations).isNotEmpty();
        assertThat(violations.toString()).contains("Не указан статус (available) вещи!");
    }
}
