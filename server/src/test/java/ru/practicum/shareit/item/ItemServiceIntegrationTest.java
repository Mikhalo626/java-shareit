package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ItemServiceIntegrationTest {

    @Autowired
    private ItemService itemService;

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
        userRepository.deleteAll();
    }

    @Test
    void addNewItem_shouldSaveItemToDatabase() {
        User user = createUser("item-add@test.com");

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Drill");
        itemDto.setDescription("Electric drill");
        itemDto.setAvailable(true);

        ItemDto result = itemService.addNewItem(user.getId(), itemDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Drill");
        assertThat(result.getDescription()).isEqualTo("Electric drill");
        assertThat(result.getAvailable()).isTrue();

        Item savedItem = itemRepository.findById(result.getId())
                .orElseThrow();

        assertThat(savedItem.getName()).isEqualTo("Drill");
        assertThat(savedItem.getDescription()).isEqualTo("Electric drill");
        assertThat(savedItem.getAvailable()).isTrue();
        assertThat(savedItem.getOwnerId()).isEqualTo(user.getId());
    }

    @Test
    void updateItem_shouldUpdateItemInDatabase() {
        User user = createUser("item-update@test.com");

        Item item = Item.builder()
                .name("Old name")
                .description("Old description")
                .available(true)
                .ownerId(user.getId())
                .build();

        item = itemRepository.save(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("New name");
        updateDto.setDescription("New description");
        updateDto.setAvailable(false);

        ItemDto result = itemService.updateItem(
                user.getId(),
                item.getId(),
                updateDto
        );

        assertThat(result.getName()).isEqualTo("New name");
        assertThat(result.getDescription()).isEqualTo("New description");
        assertThat(result.getAvailable()).isFalse();

        Item updatedItem = itemRepository.findById(item.getId())
                .orElseThrow();

        assertThat(updatedItem.getName()).isEqualTo("New name");
        assertThat(updatedItem.getDescription()).isEqualTo("New description");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    void getItem_shouldReturnItemFromDatabase() {
        User user = createUser("item-get@test.com");

        Item item = Item.builder()
                .name("Camera")
                .description("Digital camera")
                .available(true)
                .ownerId(user.getId())
                .build();

        item = itemRepository.save(item);

        ItemDto result = itemService.getItem(user.getId(), item.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(item.getId());
        assertThat(result.getName()).isEqualTo("Camera");
        assertThat(result.getDescription()).isEqualTo("Digital camera");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    void getItems_shouldReturnUserItemsFromDatabase() {
        User user = createUser("item-list@test.com");

        Item firstItem = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(user.getId())
                .build();

        Item secondItem = Item.builder()
                .name("Hammer")
                .description("Metal hammer")
                .available(true)
                .ownerId(user.getId())
                .build();

        itemRepository.saveAll(List.of(firstItem, secondItem));

        List<ItemDto> result = itemService.getItems(user.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Drill", "Hammer");
    }

    @Test
    void searchItems_shouldReturnAvailableMatchingItems() {
        User owner = createUser("item-search@test.com");

        Item availableItem = Item.builder()
                .name("Power drill")
                .description("Electric tool")
                .available(true)
                .ownerId(owner.getId())
                .build();

        Item unavailableItem = Item.builder()
                .name("Old drill")
                .description("Electric tool")
                .available(false)
                .ownerId(owner.getId())
                .build();

        Item unrelatedItem = Item.builder()
                .name("Hammer")
                .description("Metal tool")
                .available(true)
                .ownerId(owner.getId())
                .build();

        itemRepository.saveAll(
                List.of(availableItem, unavailableItem, unrelatedItem)
        );

        List<ItemDto> result = itemService.searchItems("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Power drill");
    }

    @Test
    void updateItem_shouldThrowWhenUserIsNotOwner() {
        User owner = createUser("item-owner@test.com");
        User anotherUser = createUser("item-another@test.com");

        Item item = Item.builder()
                .name("Camera")
                .description("Digital camera")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        ItemDto updateDto = new ItemDto();
        updateDto.setName("Changed camera");

        long itemId = item.getId();

        assertThatThrownBy(() ->
                itemService.updateItem(anotherUser.getId(), itemId, updateDto)
        )
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Изменять вещь может только её владелец");
    }

    private User createUser(String email) {
        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        return userRepository.save(user);
    }
}