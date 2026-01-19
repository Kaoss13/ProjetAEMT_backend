package com.helha.projetaemt_backend.application.note.query.getbyid;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetByIdNoteHandler {
    private final INoteRepository noteRepository;

    public GetByIdNoteHandler(INoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public GetByIdNoteOutput handle(int id){
        DbNote entity = noteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Note not found"));

            GetByIdNoteOutput out = new GetByIdNoteOutput();

            out.id = entity.id;
            out.idUser = entity.user != null ? entity.user.id : 0;
            out.idFolder = Math.toIntExact(entity.folder != null ? entity.folder.id : 0);

            out.title = entity.title;
            out.content = entity.content;
            out.createdAt = entity.createdAt;
            out.updatedAt = entity.updatedAt;
            out.sizeBytes = entity.sizeBytes;
            out.lineCount = entity.lineCount;
            out.wordCount = entity.wordCount;
            out.charCount = entity.charCount;

            return out;

    }
}
