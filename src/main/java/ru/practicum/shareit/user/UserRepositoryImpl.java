package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private static final long INITIAL_ID = 1L;

    private final List<User> users = new ArrayList<>();
    private long nextId = INITIAL_ID;

    @Override
    public List<User> findAll() {
        return users;
    }

    @Override
    public User findById(long userId) {
        return users.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public User save(User user) {
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    @Override
    public User update(User user) {
        User existingUser = findById(user.getId());

        if (existingUser != null) {
            if (user.getName() != null) {
                existingUser.setName(user.getName());
            }

            if (user.getEmail() != null) {
                existingUser.setEmail(user.getEmail());
            }
        }

        return existingUser;
    }

    @Override
    public void deleteById(long userId) {
        users.removeIf(user -> user.getId().equals(userId));
    }
}