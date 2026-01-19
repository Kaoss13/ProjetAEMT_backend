package com.helha.projetaemt_backend.application.note.query.getbyid;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetByIdNoteHandler {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;

    public GetByIdNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
    }

    public GetByIdNoteOutput handle(int id){
        Optional<DbNote> entity = noteRepository.findById(id);

        if (entity.isPresent())
            return modelMapper.map(entity.get(), GetByIdNoteOutput.class);

        throw new IllegalArgumentException("Note not found");
    }
}
