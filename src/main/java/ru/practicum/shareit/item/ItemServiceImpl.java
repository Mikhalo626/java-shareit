package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto addNewItem(long userId, ItemDto itemDto) {
        log.info("Добавление новой вещи пользователем с id {}", userId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название вещи обязательно"
            );
        }

        if (itemDto.getDescription() == null
                || itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Описание вещи обязательно"
            );
        }

        if (itemDto.getAvailable() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Поле available обязательно"
            );
        }

        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequestId(itemDto.getRequestId());

        Item savedItem = itemRepository.save(item);

        return itemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(long userId, long itemId, ItemDto itemDto) {
        log.info(
                "Обновление вещи с id {} пользователем с id {}",
                itemId,
                userId
        );

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Вещь не найдена"
                        )
                );

        if (!existingItem.getOwner().getId().equals(userId)) {
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

        Item updatedItem = itemRepository.save(existingItem);

        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItem(long itemId) {
        log.info("Получение вещи с id {}", itemId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Вещь не найдена"
                        )
                );

        ItemDto itemDto = itemMapper.toItemDto(item);

        List<Comment> comments = commentRepository
                .findByItem_IdOrderByCreatedDesc(itemId);

        itemDto.setComments(
                comments.stream()
                        .map(this::toCommentDto)
                        .toList()
        );

        return itemDto;
    }

    @Override
    public List<ItemDto> getItems(long userId) {
        log.info("Получение списка вещей пользователя с id {}", userId);

        List<Item> items = itemRepository.findByOwner_Id(userId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .toList();

        List<Comment> comments = commentRepository
                .findByItem_IdInOrderByCreatedDesc(itemIds);

        Map<Long, List<CommentDto>> commentsByItem = new HashMap<>();

        for (Comment comment : comments) {
            commentsByItem
                    .computeIfAbsent(
                            comment.getItem().getId(),
                            key -> new java.util.ArrayList<>()
                    )
                    .add(toCommentDto(comment));
        }

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = itemMapper.toItemDto(item);
                    itemDto.setComments(
                            commentsByItem.getOrDefault(
                                    item.getId(),
                                    List.of()
                            )
                    );
                    return itemDto;
                })
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

    @Override
    public CommentDto addComment(
            long userId,
            long itemId,
            CommentDto commentDto) {

        log.info(
                "Добавление комментария пользователем {} к вещи {}",
                userId,
                itemId
        );

        User author = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        )
                );

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Вещь не найдена"
                        )
                );

        if (commentDto.getText() == null
                || commentDto.getText().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Текст комментария обязателен"
            );
        }

        boolean canComment = bookingRepository
                .existsByItem_IdAndBooker_IdAndEndBeforeAndStatus(
                        itemId,
                        userId,
                        LocalDateTime.now(),
                        BookingStatus.APPROVED
                );

        if (!canComment) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Пользователь не брал эту вещь в аренду"
            );
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return toCommentDto(savedComment);
    }

    private CommentDto toCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthorName(comment.getAuthor().getName());
        commentDto.setCreated(comment.getCreated());

        return commentDto;
    }
}