package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class UserClient {

    private final RestClient restClient;

    public UserClient(@Value("${server.url}") String serverUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public List<UserDto> getAllUsers() {
        log.info("Запрос списка пользователей на сервер");

        UserDto[] users = restClient.get()
                .uri("/users")
                .retrieve()
                .body(UserDto[].class);

        return users == null ? List.of() : Arrays.asList(users);
    }

    public UserDto getUserById(long userId) {
        log.info("Запрос пользователя с id {} на сервер", userId);

        return restClient.get()
                .uri("/users/{userId}", userId)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return response.bodyTo(UserDto.class);
                    }

                    if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Пользователь не найден"
                        );
                    }

                    throw new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "Ошибка при обращении к серверу"
                    );
                });
    }

    public UserDto createUser(UserDto userDto) {
        log.info("Запрос на создание пользователя с email {} на сервер", userDto.getEmail());

        return restClient.post()
                .uri("/users")
                .body(userDto)
                .retrieve()
                .body(UserDto.class);
    }

    public UserDto updateUser(long userId, UserDto userDto) {
        log.info("Запрос на обновление пользователя с id {} на сервер", userId);

        return restClient.patch()
                .uri("/users/{userId}", userId)
                .body(userDto)
                .retrieve()
                .body(UserDto.class);
    }

    public void deleteUser(long userId) {
        log.info("Запрос на удаление пользователя с id {} на сервер", userId);

        restClient.delete()
                .uri("/users/{userId}", userId)
                .retrieve()
                .toBodilessEntity();
    }
}