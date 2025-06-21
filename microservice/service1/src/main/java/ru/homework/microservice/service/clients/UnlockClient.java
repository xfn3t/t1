package ru.homework.microservice.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.homework.microservice.config.FeignAuthConfig;
import ru.homework.microservice.dto.response.UnlockResponse;

import java.util.UUID;

@FeignClient(name = "unlock", url = "${service3.url}", configuration = FeignAuthConfig.class)
public interface UnlockClient {
    @PostMapping("/api/unblock/clients/{clientId}")
    UnlockResponse unblockClient(@PathVariable("clientId") UUID clientId);

    @PostMapping("/api/unblock/accounts/{accountId}")
    UnlockResponse unblockAccount(@PathVariable("accountId") String accountId);
}
