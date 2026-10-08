package ru.practicum.shareit.itemrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto createRequest(long userId, ItemRequestDto requestDto) {
        log.info("Создание запроса на вещь пользователем с id {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        ItemRequest request = new ItemRequest();
        request.setDescription(requestDto.getDescription());
        request.setRequesterId(userId);
        request.setCreated(LocalDateTime.now());

        ItemRequest saved = itemRequestRepository.save(request);

        return toDto(saved);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(long userId) {
        log.info("Получение запросов пользователя с id {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId);

        return toDtosWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId) {
        log.info("Получение запросов других пользователей для пользователя с id {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        List<ItemRequest> requests = itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId);

        return toDtosWithItems(requests);
    }

    @Override
    public ItemRequestDto getRequest(long userId, long requestId) {
        log.info(
                "Получение запроса с id {} пользователем с id {}",
                requestId,
                userId
        );

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Запрос не найден"
                ));

        ItemRequestDto dto = toDto(request);

        List<ItemDto> items = itemRepository
                .findAllByRequestIdOrderByIdAsc(requestId)
                .stream()
                .map(itemMapper::toItemDto)
                .toList();

        dto.setItems(items);

        return dto;
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        List<Item> items = itemRepository
                .findAllByRequestIdInOrderByRequestIdAscIdAsc(requestIds);

        Map<Long, List<ItemDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(
                                itemMapper::toItemDto,
                                Collectors.toList()
                        )
                ));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = toDto(request);
                    dto.setItems(
                            itemsByRequestId.getOrDefault(
                                    request.getId(),
                                    List.of()
                            )
                    );
                    return dto;
                })
                .toList();
    }

    private ItemRequestDto toDto(ItemRequest request) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setRequesterId(request.getRequesterId());
        dto.setCreated(request.getCreated());
        return dto;
    }
}