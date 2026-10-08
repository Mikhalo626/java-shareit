package ru.practicum.shareit.item;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemClientTest {

    private MockRestServiceServer server;
    private ItemClient itemClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        itemClient = new ItemClient(builder.build());
        objectMapper = new ObjectMapper();
    }

    @Test
    void addItem_shouldSendPostRequest()
            throws JsonProcessingException {
        ItemDto responseDto = createItem(10L, "Drill");

        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        ItemDto item = createItem(null, "Drill");

        ItemDto result = itemClient.addItem(1L, item);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Drill");

        server.verify();
    }

    @Test
    void getItem_shouldSendUserHeader()
            throws JsonProcessingException {
        ItemDto responseDto = createItem(10L, "Drill");

        server.expect(requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        ItemDto result = itemClient.getItem(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getOwnerId()).isEqualTo(1L);

        server.verify();
    }

    @Test
    void getItem_shouldWorkWithoutUserHeader()
            throws JsonProcessingException {
        ItemDto responseDto = createItem(10L, "Drill");

        server.expect(requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        ItemDto result = itemClient.getItem(null, 10L);

        assertThat(result.getId()).isEqualTo(10L);

        server.verify();
    }

    @Test
    void getItems_shouldReturnItems()
            throws JsonProcessingException {
        List<ItemDto> response = List.of(
                createItem(10L, "Drill"),
                createItem(11L, "Saw")
        );

        server.expect(requestTo("http://localhost:9090/items"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(response),
                        MediaType.APPLICATION_JSON
                ));

        List<ItemDto> result = itemClient.getItems(1L);

        assertThat(result)
                .hasSize(2)
                .extracting(ItemDto::getId)
                .containsExactly(10L, 11L);

        server.verify();
    }

    @Test
    void updateItem_shouldSendPatchRequest()
            throws JsonProcessingException {
        ItemDto responseDto = createItem(10L, "Updated drill");

        server.expect(requestTo("http://localhost:9090/items/10"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        ItemDto item = createItem(null, "Updated drill");

        ItemDto result =
                itemClient.updateItem(1L, 10L, item);

        assertThat(result.getName()).isEqualTo("Updated drill");

        server.verify();
    }

    @Test
    void searchItems_shouldSendSearchRequest()
            throws JsonProcessingException {
        ItemDto responseDto = createItem(10L, "Drill");

        server.expect(requestTo(
                        "http://localhost:9090/items/search?text=drill"
                ))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("text", "drill"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(
                                List.of(responseDto)
                        ),
                        MediaType.APPLICATION_JSON
                ));

        List<ItemDto> result = itemClient.searchItems("drill");

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(ItemDto::getName)
                .isEqualTo("Drill");

        server.verify();
    }

    private ItemDto createItem(Long id, String name) {
        ItemDto item = new ItemDto();
        item.setId(id);
        item.setName(name);
        item.setDescription("Power drill");
        item.setAvailable(true);
        item.setOwnerId(1L);
        return item;
    }
}