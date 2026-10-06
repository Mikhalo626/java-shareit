package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingResponseDto createBooking(long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователем с id {}", userId);

        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Вещь не найдена"
                ));

        if (item.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя забронировать собственную вещь"
            );
        }

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Вещь недоступна для бронирования"
            );
        }

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата начала и дата окончания обязательны"
            );
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата окончания должна быть позже даты начала"
            );
        }

        if (!bookingDto.getStart().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата начала должна быть в будущем"
            );
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return bookingMapper.toBookingResponseDto(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingResponseDto approveBooking(
            long userId,
            long bookingId,
            boolean approved) {

        log.info(
                "Изменение статуса бронирования с id {} пользователем с id {}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Бронирование не найдено"
                ));

        if (!booking.getItem().getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Только владелец вещи может подтвердить бронирование"
            );
        }

        booking.setStatus(
                approved ? BookingStatus.APPROVED : BookingStatus.REJECTED
        );

        return bookingMapper.toBookingResponseDto(
                bookingRepository.save(booking)
        );
    }

    @Override
    public BookingResponseDto getBooking(long userId, long bookingId) {
        log.info(
                "Получение бронирования с id {} пользователем с id {}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Бронирование не найдено"
                ));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwnerId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Нет доступа к этому бронированию"
            );
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(
            long userId,
            BookingQueryState state) {

        log.info(
                "Получение бронирований пользователя с id {} со статусом {}",
                userId,
                state
        );

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        List<Booking> bookings =
                bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);

        return filterByState(bookings, state).stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(
            long userId,
            BookingQueryState state) {

        log.info(
                "Получение бронирований вещей владельца с id {} со статусом {}",
                userId,
                state
        );

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        List<Booking> bookings =
                bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(userId);

        return filterByState(bookings, state).stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }

    private List<Booking> filterByState(
            List<Booking> bookings,
            BookingQueryState state) {

        if (state == BookingQueryState.ALL) {
            return bookings;
        }

        LocalDateTime now = LocalDateTime.now();

        Predicate<Booking> predicate = switch (state) {
            case CURRENT ->
                    booking -> booking.getStart().isBefore(now)
                            && booking.getEnd().isAfter(now);

            case PAST ->
                    booking -> booking.getEnd().isBefore(now);

            case FUTURE ->
                    booking -> booking.getStart().isAfter(now);

            case WAITING ->
                    booking -> booking.getStatus() == BookingStatus.WAITING;

            case REJECTED ->
                    booking -> booking.getStatus() == BookingStatus.REJECTED;

            case ALL -> booking -> true;
        };

        return bookings.stream()
                .filter(predicate)
                .toList();
    }
}