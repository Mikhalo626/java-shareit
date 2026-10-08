package ru.practicum.shareit.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;

    @NotBlank
    private String text;

    private Long itemId;
    private Long authorId;
    private String authorName;
    private LocalDateTime created;
}