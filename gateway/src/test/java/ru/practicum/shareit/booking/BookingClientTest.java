package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {

    private MockRestServiceServer server;
    private BookingClient bookingClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        bookingClient = new BookingClient(builder.build());
        objectMapper = new ObjectMapper();
    }

    @Test
    void createBooking_shouldSendPostRequest()
            throws JsonProcessingException {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(10L);
        responseDto.setStatus(BookingStatus.WAITING);

        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(5L);
        bookingDto.setStart(
                LocalDateTime.of(2026, 10, 10, 10, 0)
        );
        bookingDto.setEnd(
                LocalDateTime.of(2026, 10, 10, 12, 0)
        );

        BookingResponseDto result =
                bookingClient.createBooking(1L, bookingDto);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);

        server.verify();
    }

    @Test
    void approveBooking_shouldSendPatchRequest()
            throws JsonProcessingException {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(10L);
        responseDto.setStatus(BookingStatus.APPROVED);

        server.expect(
                        requestTo(
                                "http://localhost:9090/bookings/10"
                                        + "?approved=true"
                        )
                )
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(queryParam("approved", "true"))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        BookingResponseDto result =
                bookingClient.approveBooking(1L, 10L, true);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus())
                .isEqualTo(BookingStatus.APPROVED);

        server.verify();
    }

    @Test
    void getBooking_shouldSendGetRequest()
            throws JsonProcessingException {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(10L);
        responseDto.setStatus(BookingStatus.APPROVED);

        server.expect(
                        requestTo("http://localhost:9090/bookings/10")
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(responseDto),
                        MediaType.APPLICATION_JSON
                ));

        BookingResponseDto result =
                bookingClient.getBooking(1L, 10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus())
                .isEqualTo(BookingStatus.APPROVED);

        server.verify();
    }

    @Test
    void getUserBookings_shouldReturnBookings()
            throws JsonProcessingException {
        BookingResponseDto first = new BookingResponseDto();
        first.setId(10L);
        first.setStatus(BookingStatus.APPROVED);

        BookingResponseDto second = new BookingResponseDto();
        second.setId(11L);
        second.setStatus(BookingStatus.WAITING);

        List<BookingResponseDto> response = List.of(first, second);

        server.expect(
                        requestTo(
                                "http://localhost:9090/bookings"
                                        + "?state=ALL"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("state", "ALL"))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(response),
                        MediaType.APPLICATION_JSON
                ));

        List<BookingResponseDto> result =
                bookingClient.getUserBookings(
                        1L,
                        BookingQueryState.ALL
                );

        assertThat(result)
                .hasSize(2)
                .extracting(BookingResponseDto::getId)
                .containsExactly(10L, 11L);

        server.verify();
    }

    @Test
    void getOwnerBookings_shouldReturnBookings()
            throws JsonProcessingException {
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(20L);
        responseDto.setStatus(BookingStatus.APPROVED);

        server.expect(
                        requestTo(
                                "http://localhost:9090/bookings/owner"
                                        + "?state=ALL"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("state", "ALL"))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        objectMapper.writeValueAsString(
                                List.of(responseDto)
                        ),
                        MediaType.APPLICATION_JSON
                ));

        List<BookingResponseDto> result =
                bookingClient.getOwnerBookings(
                        1L,
                        BookingQueryState.ALL
                );

        assertThat(result)
                .hasSize(1)
                .first()
                .extracting(BookingResponseDto::getId)
                .isEqualTo(20L);

        server.verify();
    }

    @Test
    void constructor_shouldCreateClient() {
        BookingClient client =
                new BookingClient("http://localhost:9090");

        assertThat(client).isNotNull();
    }

    @Test
    void getUserBookings_shouldReturnEmptyListWhenResponseIsNull() {
        server.expect(
                        requestTo(
                                "http://localhost:9090/bookings"
                                        + "?state=ALL"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("state", "ALL"))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        List<BookingResponseDto> result =
                bookingClient.getUserBookings(
                        1L,
                        BookingQueryState.ALL
                );

        assertThat(result).isEmpty();

        server.verify();
    }

    @Test
    void getOwnerBookings_shouldReturnEmptyListWhenResponseIsNull() {
        server.expect(
                        requestTo(
                                "http://localhost:9090/bookings/owner"
                                        + "?state=ALL"
                        )
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("state", "ALL"))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess());

        List<BookingResponseDto> result =
                bookingClient.getOwnerBookings(
                        1L,
                        BookingQueryState.ALL
                );

        assertThat(result).isEmpty();

        server.verify();
    }
}