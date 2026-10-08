package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class UserClientTest {

    private MockRestServiceServer server;
    private UserClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("http://localhost:9090");

        server = MockRestServiceServer.bindTo(builder).build();
        client = new UserClient(builder.build());
    }

    @Test
    void getUserById_shouldReturnUser() {
        String response = """
                {
                  "id": 1,
                  "name": "Михаил",
                  "email": "mihail@example.com"
                }
                """;

        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        UserDto result = client.getUserById(1);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Михаил");
        assertThat(result.getEmail()).isEqualTo("mihail@example.com");
    }

    @Test
    void deleteUser_shouldSendDeleteRequest() {
        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(DELETE))
                .andRespond(withNoContent());

        client.deleteUser(1);

        server.verify();
    }

    @Test
    void updateUser_shouldSendPatchRequest() {
        String response = """
                {
                  "id": 1,
                  "name": "Михаил",
                  "email": "mihail@example.com"
                }
                """;

        server.expect(requestTo("http://localhost:9090/users/1"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        UserDto request = new UserDto();
        request.setName("Михаил");

        UserDto result = client.updateUser(1, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Михаил");
    }

    @Test
    void getUserById_shouldThrowNotFound() {
        server.expect(requestTo("http://localhost:9090/users/999"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getUserById(999))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getAllUsers_shouldReturnUsers() {
        String response = """
                [
                  {
                    "id": 1,
                    "name": "Михаил",
                    "email": "mihail@example.com"
                  }
                ]
                """;

        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(GET))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        var result = client.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
    }

    @Test
    void createUser_shouldSendPostRequest() {
        String response = """
                {
                  "id": 1,
                  "name": "Михаил",
                  "email": "mihail@example.com"
                }
                """;

        server.expect(requestTo("http://localhost:9090/users"))
                .andExpect(method(POST))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        UserDto request = new UserDto();
        request.setName("Михаил");
        request.setEmail("mihail@example.com");

        UserDto result = client.createUser(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("mihail@example.com");
    }
}