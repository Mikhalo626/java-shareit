package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserDto create(@RequestBody UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User savedUser = userService.createUser(user);

        return UserMapper.toUserDto(savedUser);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAllUsers().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @GetMapping("/{userId}")
    public UserDto getById(@PathVariable long userId) {
        User user = userService.getUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId,
                          @RequestBody UserDto userDto) {
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User updatedUser = userService.updateUser(userId, user);

        return UserMapper.toUserDto(updatedUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        userService.deleteUser(userId);
    }
}
