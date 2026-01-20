package com.helha.projetaemt_backend.application.folder.command.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateFolderInput {
    public int userId;
    @NotBlank
    @Size(max = 255)
    public String title;
    public Integer parentFolderId; //Integer car cet attribut peut-être null
}
