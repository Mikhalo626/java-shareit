package ru.practicum.shareit.itemrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ItemRequestServiceIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createRequest_shouldSaveRequestToDatabase() {
        User user = createUser("request-create@test.com");

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequestDto result = itemRequestService.createRequest(
                user.getId(),
                requestDto
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Need a drill");
        assertThat(result.getRequesterId()).isEqualTo(user.getId());
        assertThat(result.getCreated()).isNotNull();

        ItemRequest savedRequest = itemRequestRepository
                .findById(result.getId())
                .orElseThrow();

        assertThat(savedRequest.getDescription()).isEqualTo("Need a drill");
        assertThat(savedRequest.getRequesterId()).isEqualTo(user.getId());
        assertThat(savedRequest.getCreated()).isNotNull();
    }

    @Test
    void getUserRequests_shouldReturnRequestsWithItemsOrderedByCreatedDesc() {
        User user = createUser("request-list@test.com");

        ItemRequest firstRequest = createRequest(
                user.getId(),
                "First request",
                LocalDateTime.now().minusMinutes(10)
        );

        ItemRequest secondRequest = createRequest(
                user.getId(),
                "Second request",
                LocalDateTime.now()
        );

        createItem(
                "First item",
                "Item for first request",
                user.getId(),
                firstRequest.getId()
        );

        createItem(
                "Second item",
                "Item for second request",
                user.getId(),
                secondRequest.getId()
        );

        List<ItemRequestDto> result =
                itemRequestService.getUserRequests(user.getId());

        assertThat(result)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Second request", "First request");

        assertThat(result.get(0).getItems())
                .extracting(ItemDto::getName)
                .containsExactly("Second item");

        assertThat(result.get(1).getItems())
                .extracting(ItemDto::getName)
                .containsExactly("First item");
    }

    @Test
    void getAllRequests_shouldExcludeCurrentUserAndReturnItems() {
        User currentUser = createUser("request-current@test.com");
        User anotherUser = createUser("request-another@test.com");

        ItemRequest currentUserRequest = createRequest(
                currentUser.getId(),
                "Current user's request",
                LocalDateTime.now()
        );

        ItemRequest anotherUserRequest = createRequest(
                anotherUser.getId(),
                "Another user's request",
                LocalDateTime.now().minusMinutes(1)
        );

        createItem(
                "Another user's item",
                "Item for another user request",
                currentUser.getId(),
                anotherUserRequest.getId()
        );

        createItem(
                "Current user's item",
                "Item for current user request",
                currentUser.getId(),
                currentUserRequest.getId()
        );

        List<ItemRequestDto> result =
                itemRequestService.getAllRequests(currentUser.getId());

        assertThat(result)
                .extracting(ItemRequestDto::getDescription)
                .containsExactly("Another user's request");

        assertThat(result)
                .extracting(ItemRequestDto::getRequesterId)
                .containsExactly(anotherUser.getId());

        assertThat(result.get(0).getItems())
                .extracting(ItemDto::getName)
                .containsExactly("Another user's item");
    }

    @Test
    void getRequest_shouldReturnRequestWithRelatedItems() {
        User requester = createUser("request-get@test.com");

        ItemRequest request = createRequest(
                requester.getId(),
                "Need a camera",
                LocalDateTime.now()
        );

        createItem(
                "Camera",
                "Digital camera",
                requester.getId(),
                request.getId()
        );

        createItem(
                "Action camera",
                "Small camera",
                requester.getId(),
                request.getId()
        );

        ItemRequestDto result = itemRequestService.getRequest(
                requester.getId(),
                request.getId()
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(request.getId());
        assertThat(result.getDescription()).isEqualTo("Need a camera");
        assertThat(result.getRequesterId()).isEqualTo(requester.getId());
        assertThat(result.getItems()).hasSize(2);

        assertThat(result.getItems())
                .extracting(ItemDto::getName)
                .containsExactly("Camera", "Action camera");
    }

    @Test
    void getRequest_shouldReturnEmptyItemsWhenRequestHasNoResponses() {
        User user = createUser("request-empty@test.com");

        ItemRequest request = createRequest(
                user.getId(),
                "Need something",
                LocalDateTime.now()
        );

        ItemRequestDto result = itemRequestService.getRequest(
                user.getId(),
                request.getId()
        );

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void getRequest_shouldThrowWhenRequestDoesNotExist() {
        User user = createUser("request-not-found@test.com");

        assertThatThrownBy(() ->
                itemRequestService.getRequest(user.getId(), 999999L)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Запрос не найден");
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return userRepository.save(user);
    }

    private ItemRequest createRequest(
            Long userId,
            String description,
            LocalDateTime created) {

        ItemRequest request = new ItemRequest();
        request.setDescription(description);
        request.setRequesterId(userId);
        request.setCreated(created);

        return itemRequestRepository.save(request);
    }

    private Item createItem(
            String name,
            String description,
            Long ownerId,
            Long requestId) {

        Item item = Item.builder()
                .name(name)
                .description(description)
                .available(true)
                .ownerId(ownerId)
                .requestId(requestId)
                .build();

        return itemRepository.save(item);
    }
}