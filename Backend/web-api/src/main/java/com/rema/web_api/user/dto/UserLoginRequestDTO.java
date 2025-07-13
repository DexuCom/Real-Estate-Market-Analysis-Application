package com.rema.web_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginRequestDTO (
        @NotBlank(message="Nazwa użytkownika nie może być pusta!")
        @Size(min=3, max=40, message="Nazwa użytkownika musi mieć od 3 do 40 znaków")
        String username,

        @NotBlank(message="Hasło użytkownika nie może być puste!")
        @Size(min=8, message="Hasło użytkownika musi mieć co najmniej 8 znaków")
        String password
) {}