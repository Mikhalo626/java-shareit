package ru.practicum.shareit.booking;

import org.mapstruct.Mapper;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponseDto toBookingResponseDto(Booking booking);
}