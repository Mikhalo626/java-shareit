package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    @PostMapping
    public UserDto createUser(@RequestBody User user) {
        return userMapper.toUserDto(
                userService.createUser(user)
        );
    }

    @PatchMapping
    public UserDto updateUser(@RequestHeader("X-Sharer-User-Id") long userId,
                              @RequestBody User user) {
        return userMapper.toUserDto(
                userService.updateUser(userId, user)
        );
    }

    @GetMapping("/{userId}")
    public UserDto getUser(@PathVariable long userId) {
        return userMapper.toUserDto(
                userService.getUserById(userId)
        );
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        userService.deleteUser(userId);
    }
}