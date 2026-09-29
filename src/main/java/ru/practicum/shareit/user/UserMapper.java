package ru.practicum.shareit.user;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        return dto;
    }
}