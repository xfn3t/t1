package ru.homework.microservice.mapper;

import org.mapstruct.Mapper;
import ru.homework.microservice.model.user.User;
import ru.homework.microservice.dto.request.UserRequestDto;
import ru.homework.microservice.dto.response.UserResponseDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
