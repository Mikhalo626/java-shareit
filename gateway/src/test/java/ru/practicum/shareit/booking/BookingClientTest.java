package ru.practicum.shareit.booking;

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

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        bookingClient = new BookingClient(builder.build());
    }

    @Test
    void createBooking_shouldSendPostRequest() {
        server.expect(requestTo("http://localhost:9090/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 10,
                          "status": "WAITING"
                        }
                        """,
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
    void approveBooking_shouldSendPatchRequest() {
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
                        """
                        {
                          "id": 10,
                          "status": "APPROVED"
                        }
                        """,
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
    void getBooking_shouldSendGetRequest() {
        server.expect(
                        requestTo("http://localhost:9090/bookings/10")
                )
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess(
                        """
                        {
                          "id": 10,
                          "status": "APPROVED"
                        }
                        """,
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
    void getUserBookings_shouldReturnBookings() {
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
                        """
                        [
                          {
                            "id": 10,
                            "status": "APPROVED"
                          },
                          {
                            "id": 11,
                            "status": "WAITING"
                          }
                        ]
                        """,
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
    void getOwnerBookings_shouldReturnBookings() {
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
                        """
                        [
                          {
                            "id": 20,
                            "status": "APPROVED"
                          }
                        ]
                        """,
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
}