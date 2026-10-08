package ru.practicum.shareit.itemrequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemRequestClientTest {

    private MockRestServiceServer server;
    private ItemRequestClient itemRequestClient;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        itemRequestClient = new ItemRequestClient(builder.build());
    }

    @Test
    void createRequest_shouldSendPostRequest() {
        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 10,
                          "description": "Need a drill",
                          "requesterId": 1
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Need a drill");

        ItemRequestDto result =
                itemRequestClient.createRequest(1L, requestDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription())
                .isEqualTo("Need a drill");
        assertThat(result.getRequesterId()).isEqualTo(1L);

        server.verify();
    }

    @Test
    void getUserRequests_shouldReturnRequests() {
        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        [
                          {
                            "id": 10,
                            "description": "Need a drill",
                            "requesterId": 1
                          },
                          {
                            "id": 11,
                            "description": "Need a saw",
                            "requesterId": 1
                          }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        List<ItemRequestDto> result =
                itemRequestClient.getUserRequests(1L);

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getId)
                .containsExactly(10L, 11L);

        server.verify();
    }

    @Test
    void getAllRequests_shouldReturnRequests() {
        server.expect(requestTo("http://localhost:9090/requests/all"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        [
                          {
                            "id": 20,
                            "description": "Need a camera",
                            "requesterId": 2
                          },
                          {
                            "id": 21,
                            "description": "Need a tent",
                            "requesterId": 3
                          }
                        ]
                        """,
                        MediaType.APPLICATION_JSON
                ));

        List<ItemRequestDto> result =
                itemRequestClient.getAllRequests(1L);

        assertThat(result)
                .hasSize(2)
                .extracting(ItemRequestDto::getId)
                .containsExactly(20L, 21L);

        server.verify();
    }

    @Test
    void getRequest_shouldReturnRequest() {
        server.expect(requestTo("http://localhost:9090/requests/10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 10,
                          "description": "Need a drill",
                          "requesterId": 2
                        }
                        """,
                        MediaType.APPLICATION_JSON
                ));

        ItemRequestDto result =
                itemRequestClient.getRequest(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription())
                .isEqualTo("Need a drill");
        assertThat(result.getRequesterId()).isEqualTo(2L);

        server.verify();
    }
}