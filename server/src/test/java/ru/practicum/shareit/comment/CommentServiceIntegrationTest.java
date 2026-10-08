package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CommentServiceIntegrationTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void addComment_shouldSaveCommentToDatabase() {
        User owner = createUser("comment-owner@test.com");
        User booker = createUser("comment-booker@test.com");

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);

        CommentDto commentDto = new CommentDto();
        commentDto.setText("Great drill!");

        CommentDto result = commentService.addComment(
                booker.getId(),
                item.getId(),
                commentDto
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getText()).isEqualTo("Great drill!");

        Comment savedComment = commentRepository
                .findById(result.getId())
                .orElseThrow();

        assertThat(savedComment.getText()).isEqualTo("Great drill!");
        assertThat(savedComment.getItem().getId()).isEqualTo(item.getId());
        assertThat(savedComment.getAuthor().getId()).isEqualTo(booker.getId());
        assertThat(savedComment.getCreated()).isNotNull();
    }

    @Test
    void getComments_shouldReturnCommentsForItem() {
        User owner = createUser("comment-list-owner@test.com");
        User booker = createUser("comment-list-booker@test.com");

        Item item = Item.builder()
                .name("Camera")
                .description("Digital camera")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(LocalDateTime.now().minusDays(2));
        booking.setEnd(LocalDateTime.now().minusDays(1));
        booking.setStatus(BookingStatus.APPROVED);

        bookingRepository.save(booking);

        CommentDto firstComment = new CommentDto();
        firstComment.setText("First comment");

        CommentDto secondComment = new CommentDto();
        secondComment.setText("Second comment");

        commentService.addComment(
                booker.getId(),
                item.getId(),
                firstComment
        );

        commentService.addComment(
                booker.getId(),
                item.getId(),
                secondComment
        );

        List<CommentDto> result = commentService.getComments(item.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(CommentDto::getText)
                .containsExactly("Second comment", "First comment");
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return userRepository.save(user);
    }
}