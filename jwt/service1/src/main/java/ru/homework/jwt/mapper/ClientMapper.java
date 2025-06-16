package ru.homework.jwt.mapper;

import org.mapstruct.Mapper;
import ru.homework.jwt.dto.request.ClientRequestDto;
import ru.homework.jwt.dto.response.ClientResponseDto;
import ru.homework.jwt.model.user.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client toEntity(ClientRequestDto dto);
    ClientResponseDto toDto(Client entity);
}