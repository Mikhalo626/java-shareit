package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto addNewItem(long userId, ItemDto itemDto) {
        log.info("Добавление новой вещи пользователем с id {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        Item item = Item.builder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .ownerId(userId)
                .requestId(itemDto.getRequestId())
                .build();

        return itemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        log.info(
                "Обновление вещи с id {} пользователем с id {}",
                itemId,
                userId
        );

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Вещь не найдена"
                ));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Изменять вещь может только её владелец"
            );
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return itemMapper.toItemDto(itemRepository.save(existingItem));
    }

    @Override
    public ItemDto getItem(Long userId, long itemId) {
        log.info("Получение вещи с id {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Вещь не найдена"
                ));

        ItemDto dto = itemMapper.toItemDto(item);

        dto.setComments(
                commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)
                        .stream()
                        .map(commentMapper::toCommentDto)
                        .toList()
        );

        if (userId != null && item.getOwnerId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            bookingRepository
                    .findFirstByItem_IdAndStatusAndEndBeforeOrderByEndDesc(
                            itemId,
                            BookingStatus.APPROVED,
                            now
                    )
                    .ifPresent(booking ->
                            dto.setLastBooking(toBookingShortDto(booking))
                    );

            bookingRepository
                    .findFirstByItem_IdAndStatusAndStartAfterOrderByStartAsc(
                            itemId,
                            BookingStatus.APPROVED,
                            now
                    )
                    .ifPresent(booking ->
                            dto.setNextBooking(toBookingShortDto(booking))
                    );
        }

        return dto;
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        log.info("Получение списка вещей пользователя с id {}", userId);

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .toList();
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        BookingShortDto dto = new BookingShortDto();
        dto.setId(booking.getId());
        dto.setBookerId(booking.getBooker().getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        return dto;
    }
}