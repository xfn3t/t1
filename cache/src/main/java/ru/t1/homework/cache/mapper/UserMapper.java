package ru.t1.homework.cache.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.t1.homework.cache.dto.request.UserRequestDto;
import ru.t1.homework.cache.dto.response.UserResponseDto;
import ru.t1.homework.cache.model.User;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    User toEntity(UserRequestDto dto);

    UserResponseDto toDto(User entity);
}
