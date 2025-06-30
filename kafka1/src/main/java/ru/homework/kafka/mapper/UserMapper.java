package ru.homework.kafka.mapper;

import org.mapstruct.Mapper;
import ru.homework.kafka.model.User;
import ru.homework.kafka.dto.request.UserRequestDto;
import ru.homework.kafka.dto.response.UserResponseDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
