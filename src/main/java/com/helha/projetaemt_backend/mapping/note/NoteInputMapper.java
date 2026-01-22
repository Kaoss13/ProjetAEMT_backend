package com.helha.projetaemt_backend.mapping.note;

import com.helha.projetaemt_backend.application.note.command.create.CreateNoteInput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class NoteInputMapper {


    private final ModelMapper modelMapper;

    public NoteInputMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public DbNote toEntity(CreateNoteInput input, DbUser user, DbFolder folder) {

        DbNote entity = modelMapper.map(input, DbNote.class);
        entity.user = user;
        entity.folder = folder;

        LocalDateTime now = LocalDateTime.now();
        entity.createdAt = now;
        entity.updatedAt = now;


        return entity;
    }

}
