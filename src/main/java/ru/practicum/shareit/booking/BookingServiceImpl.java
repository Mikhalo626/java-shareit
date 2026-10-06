package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public BookingResponseDto createBooking(long userId, BookingDto bookingDto) {
        log.info("Создание бронирования пользователем с id {}", userId);

        User booker = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата начала и дата окончания обязательны"
            );
        }

        if (!bookingDto.getStart().isBefore(bookingDto.getEnd())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Дата начала должна быть раньше даты окончания"
            );
        }

        if (bookingDto.getItemId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST
            );
        }

        Item item = itemRepository.findByIdAndAvailableTrue(bookingDto.getItemId())
                .orElseThrow(() -> {
                    if (itemRepository.existsById(bookingDto.getItemId())) {
                        return new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Вещь недоступна для бронирования"
                        );
                    }

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Вещь не найдена"
                    );
                });

        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Нельзя забронировать собственную вещь"
            );
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
                    "Вещь уже забронирована на указанные даты"
            );
        }

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        return toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto approveBooking(
            long userId,
            long bookingId,
            boolean approved) {

        log.info(
                "Изменение статуса бронирования {} пользователем {}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Бронирование не найдено"
                        )
                );

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Изменять бронирование может только владелец вещи"
            );
        }

        booking.setStatus(
                approved
                        ? BookingStatus.APPROVED
                        : BookingStatus.REJECTED
        );

        return toResponseDto(bookingRepository.save(booking));
    }

    @Override
    public BookingResponseDto getBooking(long userId, long bookingId) {
        log.info(
                "Получение бронирования {} пользователем {}",
                bookingId,
                userId
        );

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Бронирование не найдено"
                        )
                );

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Просматривать бронирование могут только его автор и владелец вещи"
            );
        }

        return toResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getUserBookings(
            long userId,
            BookingQueryState state) {

        log.info(
                "Получение бронирований пользователя {} со статусом {}",
                userId,
                state
        );

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBooker_IdOrderByStartDesc(userId);

            case CURRENT -> bookingRepository
                    .findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                            userId,
                            now,
                            now
                    );

            case PAST -> bookingRepository
                    .findByBooker_IdAndEndBeforeOrderByStartDesc(
                            userId,
                            now
                    );

            case FUTURE -> bookingRepository
                    .findByBooker_IdAndStartAfterOrderByStartDesc(
                            userId,
                            now
                    );

            case WAITING -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(
                            userId,
                            BookingStatus.WAITING
                    );

            case REJECTED -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(
                            userId,
                            BookingStatus.REJECTED
                    );
        };

        return bookings.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public List<BookingResponseDto> getOwnerBookings(
            long userId,
            BookingQueryState state) {

        log.info(
                "Получение бронирований вещей владельца {} со статусом {}",
                userId,
                state
        );

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItem_Owner_IdOrderByStartDesc(userId);

            case CURRENT -> bookingRepository
                    .findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
                            userId,
                            now,
                            now
                    );

            case PAST -> bookingRepository
                    .findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
                            userId,
                            now
                    );

            case FUTURE -> bookingRepository
                    .findByItem_Owner_IdAndStartAfterOrderByStartDesc(
                            userId,
                            now
                    );

            case WAITING -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(
                            userId,
                            BookingStatus.WAITING
                    );

            case REJECTED -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(
                            userId,
                            BookingStatus.REJECTED
                    );
        };

        return bookings.stream()
                .map(this::toResponseDto)
                .toList();
    }

    private BookingResponseDto toResponseDto(Booking booking) {
        log.info("Формирование ответа для бронирования с id {}", booking.getId());

        BookingResponseDto response = new BookingResponseDto();

        response.setId(booking.getId());
        response.setStart(booking.getStart());
        response.setEnd(booking.getEnd());
        response.setStatus(booking.getStatus());

        ItemDto itemDto = itemMapper.toItemDto(booking.getItem());
        response.setItem(itemDto);

        response.setBooker(userMapper.toUserDto(booking.getBooker()));

        return response;
    }
}