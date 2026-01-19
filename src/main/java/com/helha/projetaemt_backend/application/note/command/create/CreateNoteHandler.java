package com.helha.projetaemt_backend.application.note.command.create;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class CreateNoteHandler {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;

    public CreateNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
    }

    public CreateNoteOutput handle(CreateNoteInput input){

    }
}
