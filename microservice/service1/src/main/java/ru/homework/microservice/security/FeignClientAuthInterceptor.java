package ru.homework.microservice.security;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.homework.microservice.config.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class FeignClientAuthInterceptor implements RequestInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public void apply(RequestTemplate template) {
        String token = tokenProvider.createToken("service1");
        template.header("Authorization", "Bearer " + token);
    }
}
