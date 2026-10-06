package ru.practicum.shareit.comment.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;
    private String text;
    private Long itemId;
    private Long authorId;
    private LocalDateTime created;
}