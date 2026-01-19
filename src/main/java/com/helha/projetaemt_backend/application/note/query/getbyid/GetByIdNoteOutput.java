package com.helha.projetaemt_backend.application.note.query.getbyid;

import java.time.LocalDateTime;

public class GetByIdNoteOutput  {

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
