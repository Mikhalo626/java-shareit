package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void createBooking_shouldSaveBookingToDatabase() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto result =
                bookingService.createBooking(booker.getId(), bookingDto);

        assertThat(result).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(item.getId());

        Booking savedBooking = bookingRepository.findById(result.getId())
                .orElseThrow();

        assertThat(savedBooking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(savedBooking.getItem().getId()).isEqualTo(item.getId());
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void approveBooking_shouldChangeBookingStatus() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner2@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker2@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto createdBooking =
                bookingService.createBooking(booker.getId(), bookingDto);

        BookingResponseDto approvedBooking =
                bookingService.approveBooking(
                        owner.getId(),
                        createdBooking.getId(),
                        true
                );

        assertThat(approvedBooking.getStatus())
                .isEqualTo(BookingStatus.APPROVED);

        Booking savedBooking = bookingRepository.findById(createdBooking.getId())
                .orElseThrow();

        assertThat(savedBooking.getStatus())
                .isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getUserBookings_shouldReturnUserBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner3@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker3@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingService.createBooking(booker.getId(), bookingDto);

        var bookings = bookingService.getUserBookings(
                booker.getId(),
                BookingQueryState.ALL
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId())
                .isEqualTo(item.getId());
    }

    @Test
    void getBooking_shouldReturnBookingForBooker() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner4@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker4@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        BookingResponseDto createdBooking =
                bookingService.createBooking(booker.getId(), bookingDto);

        BookingResponseDto result =
                bookingService.getBooking(
                        booker.getId(),
                        createdBooking.getId()
                );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(createdBooking.getId());
        assertThat(result.getItem().getId()).isEqualTo(item.getId());
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void getOwnerBookings_shouldReturnOwnerBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner5@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker5@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));

        bookingService.createBooking(booker.getId(), bookingDto);

        var bookings = bookingService.getOwnerBookings(
                owner.getId(),
                BookingQueryState.ALL
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId())
                .isEqualTo(item.getId());
    }

    @Test
    void getOwnerBookings_shouldReturnCurrentBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner6@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker6@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusHours(1));
        bookingDto.setEnd(LocalDateTime.now().plusHours(1));

        bookingService.createBooking(booker.getId(), bookingDto);

        var bookings = bookingService.getOwnerBookings(
                owner.getId(),
                BookingQueryState.CURRENT
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId())
                .isEqualTo(item.getId());
    }

    @Test
    void getOwnerBookings_shouldReturnPastBookings() {
        User owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner7@test.com");
        owner = userRepository.save(owner);

        User booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker7@test.com");
        booker = userRepository.save(booker);

        Item item = Item.builder()
                .name("Drill")
                .description("Electric drill")
                .available(true)
                .ownerId(owner.getId())
                .build();

        item = itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(item.getId());
        bookingDto.setStart(LocalDateTime.now().minusHours(3));
        bookingDto.setEnd(LocalDateTime.now().minusHours(2));

        bookingService.createBooking(booker.getId(), bookingDto);

        var bookings = bookingService.getOwnerBookings(
                owner.getId(),
                BookingQueryState.PAST
        );

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getId())
                .isEqualTo(item.getId());
    }
}