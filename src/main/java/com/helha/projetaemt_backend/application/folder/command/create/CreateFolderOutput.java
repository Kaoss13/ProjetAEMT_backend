package com.helha.projetaemt_backend.application.folder.command.create;

import java.time.LocalDateTime;

public class CreateFolderOutput {
    public int id;
    public int userId;
    public String title;
    public int parentFolderId;
    public LocalDateTime createdAt;
}
