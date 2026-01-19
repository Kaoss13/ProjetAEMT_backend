package com.helha.projetaemt_backend;

import com.helha.projetaemt_backend.application.note.command.create.CreateNoteInput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Configuration
public class NoteInputMapper {


    private final ModelMapper modelMapper;

    public NoteInputMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public DbNote toEntity(CreateNoteInput input, DbUser user, DbFolder folder) {

        // Mapper les champs simples
        DbNote entity = modelMapper.map(input, DbNote.class);

        // Mapper les relations — c’est ici que ça DOIT être fait
        entity.user = user;
        entity.folder = folder;

        // Timestamps
        LocalDateTime now = LocalDateTime.now();
        entity.createdAt = now;
        entity.updatedAt = now;

        // Métriques
        String content = input.content != null ? input.content : "";
        entity.charCount = (content.length());
        entity.sizeBytes = (content.getBytes(StandardCharsets.UTF_8).length);
        entity.lineCount = (content.isEmpty() ? 0 : content.split("\r\n|\r|\n").length);
        entity.wordCount = (content.trim().isEmpty() ? 0 : content.trim().split("\\s+").length);

        return entity;
    }

}
