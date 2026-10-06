package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = "owner")
    Optional<Item> findById(Long id);

    @EntityGraph(attributePaths = "owner")
    Optional<Item> findByIdAndAvailableTrue(Long id);

    @EntityGraph(attributePaths = "owner")
    List<Item> findByOwner_Id(Long ownerId);

    @EntityGraph(attributePaths = "owner")
    @Query("""
            SELECT i
            FROM Item i
            WHERE i.available = true
              AND (
                    LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%'))
                    OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))
                  )
            """)
    List<Item> search(@Param("text") String text);
}