package ru.practicum.shareit.itemrequest.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ItemRequestDto {

    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;
}
