package ru.homework.jwt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Отключаем CSRF, т.к. у нас REST
                .csrf(AbstractHttpConfigurer::disable)
                // Разрешаем всё по API публичных контроллеров
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/clients/**").permitAll()
                        .requestMatchers("/api/accounts/**").permitAll()
                        .requestMatchers("/api/transactions/**").permitAll()
                        // и всё остальное (в т.ч. actuator и т.п.)
                        .anyRequest().authenticated()
                )
                // Убираем дефолтную форму логина
                .httpBasic(Customizer.withDefaults())
        // Не включаем OAuth2 Resource Server здесь — он не нужен
        ;
        return http.build();
    }
}
