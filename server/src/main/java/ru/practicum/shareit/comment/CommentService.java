package ru.practicum.shareit.comment;

import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(long userId, long itemId, CommentDto commentDto);

    List<CommentDto> getComments(long itemId);
}