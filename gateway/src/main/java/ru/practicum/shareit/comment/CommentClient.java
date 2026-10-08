package ru.practicum.shareit.comment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class CommentClient {

    private final RestClient restClient;

    public CommentClient(@Value("${server.url}") String serverUrl) {
        this(RestClient.builder()
                .baseUrl(serverUrl)
                .build());
    }

    CommentClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public CommentDto addComment(
            long userId,
            long itemId,
            CommentDto commentDto) {

        log.info(
                "Добавление комментария к вещи {} пользователем {}",
                itemId,
                userId
        );

        return restClient.post()
                .uri("/items/{itemId}/comment", itemId)
                .header("X-Sharer-User-Id", String.valueOf(userId))
                .body(commentDto)
                .retrieve()
                .body(CommentDto.class);
    }

    public List<CommentDto> getComments(long itemId) {

        log.info("Получение комментариев для вещи {}", itemId);

        CommentDto[] comments = restClient.get()
                .uri("/items/{itemId}/comment", itemId)
                .retrieve()
                .body(CommentDto[].class);

        return comments == null ? List.of() : Arrays.asList(comments);
    }
}