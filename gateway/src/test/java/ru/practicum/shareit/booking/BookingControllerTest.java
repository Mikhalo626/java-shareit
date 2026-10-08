package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void getUserBookings_shouldReturnOk() throws Exception {
        when(bookingClient.getUserBookings(1L, BookingQueryState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnerBookings_shouldReturnOk() throws Exception {
        when(bookingClient.getOwnerBookings(1L, BookingQueryState.ALL))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void createBooking_shouldReturnOk() throws Exception {
        when(bookingClient.createBooking(eq(1L), any(BookingDto.class)))
                .thenReturn(null);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(LocalDateTime.of(2030, 1, 1, 10, 0));
        bookingDto.setEnd(LocalDateTime.of(2030, 1, 2, 10, 0));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingDto)))
                .andExpect(status().isOk());
    }

    @Test
    void approveBooking_shouldReturnOk() throws Exception {
        when(bookingClient.approveBooking(1L, 1L, true))
                .thenReturn(null);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    void getBooking_shouldReturnOk() throws Exception {
        when(bookingClient.getBooking(1L, 1L))
                .thenReturn(null);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }
}