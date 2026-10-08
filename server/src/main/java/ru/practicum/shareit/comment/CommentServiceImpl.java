package ru.practicum.shareit.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto addComment(
            long userId,
            long itemId,
            CommentDto commentDto) {

        log.info(
                "Добавление комментария к вещи {} пользователем {}",
                itemId,
                userId
        );

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Вещь не найдена"
                ));

        boolean completedBooking =
                bookingRepository.existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
                        itemId,
                        userId,
                        BookingStatus.APPROVED,
                        LocalDateTime.now()
                );

        if (!completedBooking) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Комментарий можно оставить только после завершённого бронирования"
            );
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(author);

        return commentMapper.toCommentDto(
                commentRepository.save(comment)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getComments(long itemId) {

        log.info("Получение комментариев для вещи {}", itemId);

        if (!itemRepository.existsById(itemId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Вещь не найдена"
            );
        }

        return commentRepository.findAllByItem_IdOrderByCreatedDesc(itemId)
                .stream()
                .map(commentMapper::toCommentDto)
                .toList();
    }
}