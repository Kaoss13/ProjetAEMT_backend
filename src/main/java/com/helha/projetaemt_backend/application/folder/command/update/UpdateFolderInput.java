package com.helha.projetaemt_backend.application.folder.command.update;

import jakarta.validation.constraints.Size;

public class UpdateFolderInput {
    public int id;
    public int userId;
    @Size(max = 255)
    public String title;
}
