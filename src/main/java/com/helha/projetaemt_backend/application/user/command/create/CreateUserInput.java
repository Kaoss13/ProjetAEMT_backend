package com.helha.projetaemt_backend.application.user.command.create;

import com.helha.projetaemt_backend.infrastructure.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class CreateUserInput {

    @Schema(example = "Username")
    @NotBlank(message = "Champ obligatoire")
    public String userName;

    @Schema(example = "MyStrongPassword!123")
    @NotBlank(message = "Champ obligatoire")
    @ValidPassword
    public String password;
}
