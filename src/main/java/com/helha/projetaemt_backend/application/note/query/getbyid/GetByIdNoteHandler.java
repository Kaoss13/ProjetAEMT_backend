package com.helha.projetaemt_backend.application.note.query.getbyid;

import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class GetByIdNoteHandler {
    private final INoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public GetByIdNoteHandler(INoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public GetByIdNoteOutput handle(int id){
        DbNote entity = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note introuvable"
                ));

        return noteMapper.map(entity, GetByIdNoteOutput.class);

    }
}
