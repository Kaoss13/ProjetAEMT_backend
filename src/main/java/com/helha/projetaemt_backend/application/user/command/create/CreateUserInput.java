package com.helha.projetaemt_backend.application.user.command.create;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateUserInput {

    @Schema(example = "Username")
    @NotBlank(message = "Champ obligatoire")
    public String userName;

    @Schema(example = "MyStrongPassword!123")
    @NotBlank(message = "Champ obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 12 caractères, 1 lettre majuscule, 1 nombre et 1 caractère spécial")
    public String password;
}
