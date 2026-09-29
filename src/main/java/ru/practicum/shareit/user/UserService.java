package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    List<User> getAllUsers();

    User getUserById(long userId);

    User createUser(User user);

    User updateUser(long userId, User user);

    void deleteUser(long userId);
}