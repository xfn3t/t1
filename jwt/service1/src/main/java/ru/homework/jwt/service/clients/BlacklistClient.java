package ru.homework.jwt.service.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.homework.jwt.config.FeignAuthConfig;
import ru.homework.jwt.dto.response.ClientStatusResponse;

@FeignClient(
        name = "blacklist",
        url = "${service2.url}",
        configuration = FeignAuthConfig.class
)
public interface BlacklistClient {
    @GetMapping("/api/clients/{clientId}/status")
    ClientStatusResponse getStatus(@PathVariable("clientId") String clientId);
}