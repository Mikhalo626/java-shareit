package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ItemClient {

    private final RestClient restClient;

    public ItemClient(@Value("${server.url}") String serverUrl) {
        this(RestClient.builder()
                .baseUrl(serverUrl)
                .build());
    }

    ItemClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ItemDto addItem(long userId, ItemDto itemDto) {
        log.info(
                "Запрос на создание вещи пользователем с id {}",
                userId
        );

        return restClient.post()
                .uri("/items")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .body(itemDto)
                .retrieve()
                .body(ItemDto.class);
    }

    public ItemDto getItem(Long userId, long itemId) {
        log.info(
                "Запрос вещи с id {} на сервер пользователем с id {}",
                itemId,
                userId
        );

        RestClient.RequestHeadersSpec<?> request = restClient.get()
                .uri("/items/{itemId}", itemId);

        if (userId != null) {
            request.header(
                    "X-Sharer-User-Id",
                    String.valueOf(userId)
            );
        }

        return request
                .retrieve()
                .body(ItemDto.class);
    }

    public List<ItemDto> getItems(long userId) {
        log.info(
                "Запрос списка вещей пользователя с id {} на сервер",
                userId
        );

        ItemDto[] items = restClient.get()
                .uri("/items")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(ItemDto[].class);

        return items == null ? List.of() : Arrays.asList(items);
    }

    public ItemDto updateItem(
            long userId,
            long itemId,
            ItemDto itemDto
    ) {
        log.info(
                "Запрос на обновление вещи с id {} пользователем с id {}",
                itemId,
                userId
        );

        return restClient.patch()
                .uri("/items/{itemId}", itemId)
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .body(itemDto)
                .retrieve()
                .body(ItemDto.class);
    }

    public List<ItemDto> searchItems(String text) {
        log.info(
                "Запрос поиска вещей по тексту: {}",
                text
        );

        ItemDto[] items = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/items/search")
                        .queryParam("text", text)
                        .build())
                .retrieve()
                .body(ItemDto[].class);

        return items == null ? List.of() : Arrays.asList(items);
    }
}