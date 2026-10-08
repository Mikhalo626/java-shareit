package ru.practicum.shareit.comment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentClient commentClient;

    @Test
    void addComment_shouldReturnOk() throws Exception {
        when(commentClient.addComment(eq(1L), eq(1L), any(CommentDto.class)))
                .thenReturn(null);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "Отличная вещь!"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturnBadRequestWhenTextIsBlank() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_shouldReturnOk() throws Exception {
        when(commentClient.getComments(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/1/comment"))
                .andExpect(status().isOk());
    }
}