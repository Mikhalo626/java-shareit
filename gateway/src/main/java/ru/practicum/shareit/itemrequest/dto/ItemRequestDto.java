package ru.practicum.shareit.itemrequest.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ItemRequestDto {

    private Long id;

    @NotBlank
    private String description;

    private Long requesterId;
    private LocalDateTime created;

    private List<ItemDto> items;
}