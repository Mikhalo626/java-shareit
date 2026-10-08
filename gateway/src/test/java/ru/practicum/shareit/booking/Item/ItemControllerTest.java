package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemClient itemClient;

    @Test
    void addItem_shouldReturnOk() throws Exception {
        when(itemClient.addItem(eq(1L), any(ItemDto.class)))
                .thenReturn(null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Дрель",
                                  "description": "Электрическая дрель",
                                  "available": true
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void getItem_shouldReturnOk() throws Exception {
        when(itemClient.getItem(1L, 1L))
                .thenReturn(null);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItems_shouldReturnOk() throws Exception {
        when(itemClient.getItems(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_shouldReturnOk() throws Exception {
        when(itemClient.updateItem(eq(1L), eq(1L), any(ItemDto.class)))
                .thenReturn(null);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Новая дрель",
                                  "description": "Обновлённое описание",
                                  "available": true
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldReturnOk() throws Exception {
        when(itemClient.searchItems("дрель"))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }
}