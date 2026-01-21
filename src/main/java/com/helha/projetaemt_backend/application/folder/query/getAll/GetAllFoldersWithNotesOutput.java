package com.helha.projetaemt_backend.application.folder.query.getAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GetAllFoldersWithNotesOutput {

    public static class FolderDto {
        public int id;
        public int userId;
        public Integer id_parent_folder;
        public String title;
    }

    public static class NoteDto {
        public int id;
        public int id_user;
        public int id_folder;
        public String title;
        public String content;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public int lineCount;
        public int wordCount;
        public int charCount;
    }

    public List<FolderDto> folders = new ArrayList<>();
    public List<NoteDto> notes = new ArrayList<>();
}
