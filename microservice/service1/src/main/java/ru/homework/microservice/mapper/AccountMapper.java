package ru.homework.microservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.homework.microservice.dto.request.AccountRequestDto;
import ru.homework.microservice.dto.response.AccountResponseDto;
import ru.homework.microservice.model.user.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "clientId", target = "client.id")
    Account toEntity(AccountRequestDto dto);
    @Mapping(source = "client.clientId", target = "clientId")
    AccountResponseDto toDto(Account entity);
}