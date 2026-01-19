package com.helha.projetaemt_backend.application.note.query.getbyidfolder;

import com.helha.projetaemt_backend.domain.note.Note;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GetByIdFolderNoteOutput {
    public List<Note> notes = new ArrayList<>();

    public static class Note {
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

}
