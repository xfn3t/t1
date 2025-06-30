package ru.homework.microservice.mapper;

import org.mapstruct.Mapper;
import ru.homework.microservice.dto.request.ClientRequestDto;
import ru.homework.microservice.dto.response.ClientResponseDto;
import ru.homework.microservice.model.user.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client toEntity(ClientRequestDto dto);
    ClientResponseDto toDto(Client entity);
}