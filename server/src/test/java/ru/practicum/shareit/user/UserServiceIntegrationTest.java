package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.itemrequest.ItemRequestRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getAllUsers_shouldReturnUsersFromDatabase() {
        User firstUser = new User();
        firstUser.setName("First User");
        firstUser.setEmail("first@test.com");

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second@test.com");

        userRepository.saveAll(List.of(firstUser, secondUser));

        List<User> result = userService.getAllUsers();

        assertThat(result)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(
                        "first@test.com",
                        "second@test.com"
                );
    }

    @Test
    void getUserById_shouldReturnUserFromDatabase() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("get-user@test.com");
        user = userRepository.save(user);

        User result = userService.getUserById(user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getEmail()).isEqualTo("get-user@test.com");
    }

    @Test
    void createUser_shouldSaveUserToDatabase() {
        User user = new User();
        user.setName("New User");
        user.setEmail("create-user@test.com");

        User result = userService.createUser(user);

        assertThat(result.getId()).isNotNull();

        User savedUser = userRepository.findById(result.getId())
                .orElseThrow();

        assertThat(savedUser.getName()).isEqualTo("New User");
        assertThat(savedUser.getEmail()).isEqualTo("create-user@test.com");
    }

    @Test
    void createUser_shouldRejectDuplicateEmail() {
        User existingUser = new User();
        existingUser.setName("Existing User");
        existingUser.setEmail("duplicate@test.com");
        userRepository.save(existingUser);

        User newUser = new User();
        newUser.setName("New User");
        newUser.setEmail("duplicate@test.com");

        assertThatThrownBy(() -> userService.createUser(newUser))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email уже используется");
    }

    @Test
    void updateUser_shouldUpdateUserInDatabase() {
        User existingUser = new User();
        existingUser.setName("Old Name");
        existingUser.setEmail("old-email@test.com");
        existingUser = userRepository.save(existingUser);

        User updateUser = new User();
        updateUser.setName("New Name");
        updateUser.setEmail("new-email@test.com");

        User result = userService.updateUser(
                existingUser.getId(),
                updateUser
        );

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getEmail()).isEqualTo("new-email@test.com");

        User updatedUser = userRepository.findById(existingUser.getId())
                .orElseThrow();

        assertThat(updatedUser.getName()).isEqualTo("New Name");
        assertThat(updatedUser.getEmail()).isEqualTo("new-email@test.com");
    }

    @Test
    void updateUser_shouldRejectDuplicateEmail() {
        User firstUser = new User();
        firstUser.setName("First User");
        firstUser.setEmail("first-update@test.com");
        firstUser = userRepository.save(firstUser);

        User secondUser = new User();
        secondUser.setName("Second User");
        secondUser.setEmail("second-update@test.com");
        secondUser = userRepository.save(secondUser);

        User updateUser = new User();
        updateUser.setEmail("first-update@test.com");

        long secondUserId = secondUser.getId();

        assertThatThrownBy(() -> userService.updateUser(secondUserId, updateUser))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email уже используется");

        User unchangedUser = userRepository.findById(secondUserId)
                .orElseThrow();

        assertThat(unchangedUser.getEmail())
                .isEqualTo("second-update@test.com");
    }

    @Test
    void deleteUser_shouldDeleteUserFromDatabase() {
        User user = new User();
        user.setName("Delete User");
        user.setEmail("delete-user@test.com");
        user = userRepository.save(user);

        long userId = user.getId();

        userService.deleteUser(userId);

        assertThat(userRepository.existsById(userId)).isFalse();
    }
}