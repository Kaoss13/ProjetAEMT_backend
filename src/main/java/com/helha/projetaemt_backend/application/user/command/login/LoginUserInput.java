package com.helha.projetaemt_backend.application.user.command.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class LoginUserInput {

    @Schema(example = "Username")
    @NotBlank(message = "Champ obligatoire")
    public String userName;

    @Schema(example = "MyStrongPassword!123")
    @NotBlank(message = "Champ obligatoire")
    public String password;
}
