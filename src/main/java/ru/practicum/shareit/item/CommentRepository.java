package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"item", "author"})
    List<Comment> findByItem_IdOrderByCreatedDesc(Long itemId);

    @EntityGraph(attributePaths = {"item", "author"})
    List<Comment> findByItem_IdInOrderByCreatedDesc(Collection<Long> itemIds);
}