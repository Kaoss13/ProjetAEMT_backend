package com.helha.projetaemt_backend.application.note.command.update;

import java.time.LocalDateTime;

public class UpdateNoteOutput {
    public int id;
    public int idUser;
    public int idFolder;
    public String title;
    public String content;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public int sizeBytes;
    public int lineCount;
    public int wordCount;
    public int charCount;
}
