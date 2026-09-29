package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    Optional<Booking> findById(Long id);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBooker_IdOrderByStartDesc(Long bookerId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBooker_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long bookerId,
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBooker_IdAndEndBeforeOrderByStartDesc(
            Long bookerId,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBooker_IdAndStartAfterOrderByStartDesc(
            Long bookerId,
            LocalDateTime start
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByBooker_IdAndStatusOrderByStartDesc(
            Long bookerId,
            BookingStatus status
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItem_Owner_IdOrderByStartDesc(Long ownerId);

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItem_Owner_IdAndStartLessThanEqualAndEndGreaterThanEqualOrderByStartDesc(
            Long ownerId,
            LocalDateTime start,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItem_Owner_IdAndEndBeforeOrderByStartDesc(
            Long ownerId,
            LocalDateTime end
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItem_Owner_IdAndStartAfterOrderByStartDesc(
            Long ownerId,
            LocalDateTime start
    );

    @EntityGraph(attributePaths = {"item", "item.owner", "booker"})
    List<Booking> findByItem_Owner_IdAndStatusOrderByStartDesc(
            Long ownerId,
            BookingStatus status
    );

    @Query("""
            SELECT COUNT(b) > 0
            FROM Booking b
            WHERE b.item.id = :itemId
              AND b.status = :status
              AND b.start < :end
              AND b.end > :start
            """)
    boolean existsOverlappingBooking(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") BookingStatus status
    );

    boolean existsByItem_IdAndBooker_IdAndEndBeforeAndStatus(
            Long itemId,
            Long bookerId,
            LocalDateTime now,
            BookingStatus status
    );

    @Query("""
            SELECT b
            FROM Booking b
            JOIN FETCH b.item
            JOIN FETCH b.booker
            WHERE b.item.id IN :itemIds
              AND b.status = :status
              AND b.end < :now
              AND b.end = (
                  SELECT MAX(previous.end)
                  FROM Booking previous
                  WHERE previous.item.id = b.item.id
                    AND previous.status = :status
                    AND previous.end < :now
              )
            """)
    List<Booking> findLastBookings(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );

    @Query("""
            SELECT b
            FROM Booking b
            JOIN FETCH b.item
            JOIN FETCH b.booker
            WHERE b.item.id IN :itemIds
              AND b.status = :status
              AND b.start > :now
              AND b.start = (
                  SELECT MIN(next.start)
                  FROM Booking next
                  WHERE next.item.id = b.item.id
                    AND next.status = :status
                    AND next.start > :now
              )
            """)
    List<Booking> findNextBookings(
            @Param("itemIds") List<Long> itemIds,
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );
}