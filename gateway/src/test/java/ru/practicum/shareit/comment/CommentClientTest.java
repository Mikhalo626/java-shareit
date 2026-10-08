package ru.practicum.shareit.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

class CommentClientTest {

    private MockRestServiceServer server;
    private CommentClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        client = new CommentClient(builder.build());
    }

    @Test
    void addComment_shouldSendRequestAndReturnComment() {
        String response = """
                {
                  "id": 1,
                  "text": "Хорошая вещь",
                  "authorName": "Михаил",
                  "created": "2026-10-08T20:00:00"
                }
                """;

        server.expect(requestTo("http://localhost:9090/items/10/comment"))
                .andExpect(method(POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        CommentDto request = new CommentDto();
        request.setText("Хорошая вещь");

        CommentDto result = client.addComment(1, 10, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Хорошая вещь");
    }

    @Test
    void getComments_shouldSendRequestAndReturnComments() {
        String response = """
                [
                  {
                    "id": 1,
                    "text": "Хорошая вещь",
                    "authorName": "Михаил",
                    "created": "2026-10-08T20:00:00"
                  }
                ]
                """;

        server.expect(requestTo("http://localhost:9090/items/10/comment"))
                .andExpect(method(GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        var result = client.getComments(10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getText()).isEqualTo("Хорошая вещь");
    }
}