package ru.practicum.shareit.itemrequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        itemRequestClient = new ItemRequestClient(builder.build());
        objectMapper = new ObjectMapper();
    }

    @Test
    void createRequest_shouldSendPostRequest()
            throws JsonProcessingException {
        ItemRequestDto responseDto =
                createRequestDto(10L, "Need a drill", 1L);

        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        ItemRequestDto requestDto =
                createRequestDto(null, "Need a drill", null);

        ItemRequestDto result =
                itemRequestClient.createRequest(1L, requestDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getDescription())
                .isEqualTo("Need a drill");
        assertThat(result.getRequesterId()).isEqualTo(1L);

        server.verify();
    }

    @Test
    void getUserRequests_shouldReturnRequests()
            throws JsonProcessingException {
        List<ItemRequestDto> response = List.of(
                createRequestDto(10L, "Need a drill", 1L),
                createRequestDto(11L, "Need a saw", 1L)
        );

        server.expect(requestTo("http://localhost:9090/requests"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(response),
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
    void getAllRequests_shouldReturnRequests()
            throws JsonProcessingException {
        List<ItemRequestDto> response = List.of(
                createRequestDto(20L, "Need a camera", 2L),
                createRequestDto(21L, "Need a tent", 3L)
        );

        server.expect(requestTo("http://localhost:9090/requests/all"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(response),
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
    void getRequest_shouldReturnRequest()
            throws JsonProcessingException {
        ItemRequestDto responseDto =
                createRequestDto(10L, "Need a drill", 2L);

        server.expect(requestTo("http://localhost:9090/requests/10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
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

    private ItemRequestDto createRequestDto(
            Long id,
            String description,
            Long requesterId) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(id);
        dto.setDescription(description);
        dto.setRequesterId(requesterId);
        return dto;
    }
}