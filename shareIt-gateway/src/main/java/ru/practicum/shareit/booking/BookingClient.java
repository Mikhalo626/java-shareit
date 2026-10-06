package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class BookingClient {

    private final RestClient restClient;

    public BookingClient(@Value("${server.url}") String serverUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public BookingResponseDto createBooking(long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователем с id {}", userId);

        return restClient.post()
                .uri("/bookings")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .body(bookingDto)
                .retrieve()
                .body(BookingResponseDto.class);
    }

    public BookingResponseDto approveBooking(
            long userId,
            long bookingId,
            boolean approved) {

        log.info(
                "Изменение бронирования {} пользователем {}",
                bookingId,
                userId
        );

        return restClient.patch()
                .uri(uriBuilder -> uriBuilder
                        .path("/bookings/{bookingId}")
                        .queryParam("approved", approved)
                        .build(bookingId))
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(BookingResponseDto.class);
    }

    public BookingResponseDto getBooking(long userId, long bookingId) {
        log.info(
                "Получение бронирования {} пользователем {}",
                bookingId,
                userId
        );

        return restClient.get()
                .uri("/bookings/{bookingId}", bookingId)
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(BookingResponseDto.class);
    }

    public List<BookingResponseDto> getUserBookings(
            long userId,
            BookingQueryState state) {

        BookingResponseDto[] bookings = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bookings")
                        .queryParam("state", state)
                        .build())
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(BookingResponseDto[].class);

        return bookings == null ? List.of() : Arrays.asList(bookings);
    }

    public List<BookingResponseDto> getOwnerBookings(
            long userId,
            BookingQueryState state) {

        BookingResponseDto[] bookings = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bookings/owner")
                        .queryParam("state", state)
                        .build())
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(BookingResponseDto[].class);

        return bookings == null ? List.of() : Arrays.asList(bookings);
    }
}