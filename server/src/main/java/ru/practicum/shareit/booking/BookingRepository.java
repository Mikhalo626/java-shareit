package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBooker_IdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItem_OwnerIdOrderByStartDesc(Long ownerId);

    @Query("""
            select b
            from Booking b
            where b.booker.id = :userId
               or b.item.ownerId = :userId
            order by b.start desc
            """)
    List<Booking> findAllByUserId(@Param("userId") Long userId);

    boolean existsByItem_IdAndBooker_IdAndStatusAndEndBefore(
            Long itemId,
            Long bookerId,
            BookingStatus status,
            LocalDateTime end);
}