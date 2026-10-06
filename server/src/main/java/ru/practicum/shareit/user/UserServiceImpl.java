package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        log.info("Получение списка всех пользователей");

        return userRepository.findAll();
    }

    @Override
    public User getUserById(long userId) {
        log.info("Получение пользователя с id {}", userId);

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Пользователь не найден"
                ));
    }

    @Override
    public User createUser(User user) {
        log.info("Создание нового пользователя с email {}", user.getEmail());

        validateEmail(user.getEmail());

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email уже используется"
            );
        }

        return userRepository.save(user);
    }

    @Override
    public User updateUser(long userId, User user) {
        log.info("Обновление пользователя с id {}", userId);

        User existingUser = getUserById(userId);

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            validateEmail(user.getEmail());

            if (!user.getEmail().equals(existingUser.getEmail())
                    && userRepository.existsByEmail(user.getEmail())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Email уже используется"
                );
            }

            existingUser.setEmail(user.getEmail());
        }

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(long userId) {
        log.info("Удаление пользователя с id {}", userId);

        getUserById(userId);
        userRepository.deleteById(userId);
    }

    private void validateEmail(String email) {
        if (email == null || !email.matches(EMAIL_REGEX)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Некорректный email"
            );
        }
    }
}