package ru.homework.microservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignAuthConfig {

    @Bean
    public RequestInterceptor authInterceptor(JwtTokenProvider prov) {
        return tmpl -> {
            String token = prov.createToken("service1");
            tmpl.header("Authorization", "Bearer "+token);
        };
    }
}
