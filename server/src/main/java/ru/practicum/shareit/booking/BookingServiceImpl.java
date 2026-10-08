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

        Item item = itemRepository.findByIdAndAvailableTrue(bookingDto.getItemId())
                .orElseThrow(() -> {
                    if (itemRepository.existsById(bookingDto.getItemId())) {
                        return new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Вещь недоступна для бронирования");
                    }

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Вещь не найдена");
                });

        if (item.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя забронировать собственную вещь");
        }

        boolean hasApprovedBooking = bookingRepository.existsOverlappingBooking(
                item.getId(),
                bookingDto.getStart(),
                bookingDto.getEnd(),
                BookingStatus.APPROVED
        );

        if (hasApprovedBooking) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Вещь уже забронирована на указанные даты");
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

        bookingRepository.save(booking);

        return bookingMapper.toBookingResponseDto(booking);
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

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL ->
                    bookingRepository.findAllByBooker_IdOrderByStartDesc(userId);

            case CURRENT ->
                    bookingRepository
                            .findAllByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                                    userId, now, now);

            case PAST ->
                    bookingRepository
                            .findAllByBooker_IdAndEndBeforeOrderByStartDesc(
                                    userId, now);

            case FUTURE ->
                    bookingRepository
                            .findAllByBooker_IdAndStartAfterOrderByStartDesc(
                                    userId, now);

            case WAITING ->
                    bookingRepository
                            .findAllByBooker_IdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.WAITING);

            case REJECTED ->
                    bookingRepository
                            .findAllByBooker_IdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
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

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL ->
                    bookingRepository.findAllByItem_OwnerIdOrderByStartDesc(userId);

            case CURRENT ->
                    bookingRepository
                            .findAllByItem_OwnerIdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                                    userId, now, now);

            case PAST ->
                    bookingRepository
                            .findAllByItem_OwnerIdAndEndBeforeOrderByStartDesc(
                                    userId, now);

            case FUTURE ->
                    bookingRepository
                            .findAllByItem_OwnerIdAndStartAfterOrderByStartDesc(
                                    userId, now);

            case WAITING ->
                    bookingRepository
                            .findAllByItem_OwnerIdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.WAITING);

            case REJECTED ->
                    bookingRepository
                            .findAllByItem_OwnerIdAndStatusOrderByStartDesc(
                                    userId, BookingStatus.REJECTED);
        };

        return bookings.stream()
                .map(bookingMapper::toBookingResponseDto)
                .toList();
    }
}