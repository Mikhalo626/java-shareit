package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BookingDto {

    private Long id;

    @NotNull
    @Future
    private LocalDateTime start;

    @NotNull
    private LocalDateTime end;

    @NotNull
    @Positive
    private Long itemId;

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    public boolean isEndAfterStart() {
        return start == null || end == null || start.isBefore(end);
    }
}