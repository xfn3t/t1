package ru.homework.jwt.mapper;

import org.mapstruct.Mapper;
import ru.homework.jwt.model.user.User;
import ru.homework.jwt.dto.request.UserRequestDto;
import ru.homework.jwt.dto.response.UserResponseDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
