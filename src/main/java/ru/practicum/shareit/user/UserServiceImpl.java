package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User getUserById(long userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }

        return user;
    }

    @Override
    public User createUser(User user) {
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
        }

        if (userRepository.findAll().stream().anyMatch(existingUser -> existingUser.getEmail().equals(user.getEmail()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(long userId, User user) {
        User existingUser = userRepository.findById(userId);

        if (existingUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден");
        }

        if (user.getEmail() != null) {
            if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректный email");
            }

            boolean emailAlreadyUsed = userRepository.findAll().stream()
                    .anyMatch(existing -> existing.getEmail().equals(user.getEmail())
                            && !existing.getId().equals(userId));
            if (emailAlreadyUsed) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email уже используется");
            }
        }

        user.setId(userId);
        return userRepository.update(user);
    }

    @Override
    public void deleteUser(long userId) {
        getUserById(userId);
        userRepository.deleteById(userId);
    }
}