package ru.homework.kafka.mapper;

import org.mapstruct.Mapper;
import ru.homework.kafka.dto.request.ClientRequestDto;
import ru.homework.kafka.dto.response.ClientResponseDto;
import ru.homework.kafka.model.user.Client;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client toEntity(ClientRequestDto dto);
    ClientResponseDto toDto(Client entity);
}