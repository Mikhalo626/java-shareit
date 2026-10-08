package ru.practicum.shareit.itemrequest;

import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto createRequest(long userId, ItemRequestDto requestDto);

    List<ItemRequestDto> getUserRequests(long userId);

    List<ItemRequestDto> getAllRequests(long userId);

    ItemRequestDto getRequest(long userId, long requestId);
}