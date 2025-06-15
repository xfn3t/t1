package ru.homework.kafka.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.homework.kafka.dto.request.TransactionRequestDto;
import ru.homework.kafka.dto.response.TransactionResponseDto;
import ru.homework.kafka.model.transaction.Transaction;


@Mapper(componentModel = "spring")
public interface TransactionMapper {
    // mapping from DTO to entity (for create)
    @Mapping(source = "accountId", target = "account.accountId")
    Transaction toEntity(TransactionRequestDto dto);
    // mapping from entity to DTO
    @Mapping(source = "account.accountId", target = "accountId")
    @Mapping(source = "account.client.clientId", target = "clientId")
    TransactionResponseDto toDto(Transaction tx);
}