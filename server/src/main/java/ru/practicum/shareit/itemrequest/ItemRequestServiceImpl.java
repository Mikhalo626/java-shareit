package ru.practicum.shareit.itemrequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.itemrequest.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto createRequest(long userId, ItemRequestDto requestDto) {
        log.info("Создание запроса на вещь пользователем с id {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        if (requestDto.getDescription() == null
                || requestDto.getDescription().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Описание запроса обязательно"
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
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        return itemRequestRepository
                .findAllByRequesterIdOrderByCreatedDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<ItemRequestDto> getAllRequests(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Пользователь не найден"
            );
        }

        return itemRequestRepository
                .findAllByRequesterIdNotOrderByCreatedDesc(userId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public ItemRequestDto getRequest(long userId, long requestId) {
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

        return toDto(request);
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