package ru.practicum.shareit.itemrequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class ItemRequestClient {

    private final RestClient restClient;

    public ItemRequestClient(@Value("${server.url}") String serverUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(serverUrl)
                .build();
    }

    public ItemRequestDto createRequest(
            long userId,
            ItemRequestDto requestDto) {

        return restClient.post()
                .uri("/requests")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .body(requestDto)
                .retrieve()
                .body(ItemRequestDto.class);
    }

    public List<ItemRequestDto> getUserRequests(long userId) {

        ItemRequestDto[] requests = restClient.get()
                .uri("/requests")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(ItemRequestDto[].class);

        return requests == null ? List.of() : Arrays.asList(requests);
    }

    public List<ItemRequestDto> getAllRequests(long userId) {

        ItemRequestDto[] requests = restClient.get()
                .uri("/requests/all")
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(ItemRequestDto[].class);

        return requests == null ? List.of() : Arrays.asList(requests);
    }

    public ItemRequestDto getRequest(long userId, long requestId) {

        return restClient.get()
                .uri("/requests/{requestId}", requestId)
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .retrieve()
                .body(ItemRequestDto.class);
    }
}