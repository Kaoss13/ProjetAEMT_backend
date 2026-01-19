package com.helha.projetaemt_backend.application.note.query.getbyidfolder;

import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class GetByIdFolderNoteHandler {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;

    public GetByIdFolderNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
    }

    public GetByIdFolderNoteOutput handle(int id){
        Iterable<DbNote> dbNotes;
        dbNotes = noteRepository.findByIdFolder(id);

        GetByIdFolderNoteOutput output = new GetByIdFolderNoteOutput();

        for (DbNote entity : dbNotes){
            output.notes.add(modelMapper.map(entity, GetByIdFolderNoteOutput.Note.class));
        }
        return output;
    }
}
