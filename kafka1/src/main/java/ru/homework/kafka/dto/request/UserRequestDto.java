package ru.t1.homework.cache.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    @NotBlank(message = "Username не пустой")
    private String username;

    @NotBlank(message = "Email не пустой")
    @Email(message = "Email невалидный")
    private String email;
}
