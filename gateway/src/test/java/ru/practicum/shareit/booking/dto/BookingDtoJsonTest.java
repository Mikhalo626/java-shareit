package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import java.util.Set;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void shouldSerializeBookingDto() throws Exception {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(LocalDateTime.of(2030, 1, 1, 10, 0));
        bookingDto.setEnd(LocalDateTime.of(2030, 1, 2, 10, 0));
        bookingDto.setItemId(2L);

        assertThat(json.write(bookingDto))
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(1);

        assertThat(json.write(bookingDto))
                .extractingJsonPathStringValue("$.start")
                .isEqualTo("2030-01-01T10:00:00");

        assertThat(json.write(bookingDto))
                .extractingJsonPathStringValue("$.end")
                .isEqualTo("2030-01-02T10:00:00");

        assertThat(json.write(bookingDto))
                .extractingJsonPathNumberValue("$.itemId")
                .isEqualTo(2);
    }

    @Test
    void shouldValidateBookingDates() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.of(2030, 1, 2, 10, 0));
        bookingDto.setEnd(LocalDateTime.of(2030, 1, 1, 10, 0));
        bookingDto.setItemId(1L);

        Set<ConstraintViolation<BookingDto>> violations = validator.validate(bookingDto);

        assertThat(violations)
                .anyMatch(violation ->
                        violation.getMessage().equals("Дата окончания должна быть позже даты начала"));
    }
}