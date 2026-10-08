package ru.practicum.shareit.itemrequest.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import ru.practicum.shareit.item.dto.ItemDto;

@Data
public class ItemRequestDto {

    private Long id;
    private String description;
    private Long requesterId;
    private LocalDateTime created;

    private List<ItemDto> items;
}