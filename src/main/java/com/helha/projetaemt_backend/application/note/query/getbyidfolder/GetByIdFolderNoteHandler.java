package com.helha.projetaemt_backend.application.note.query.getbyidfolder;

import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class GetByIdFolderNoteHandler {
    private final INoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public GetByIdFolderNoteHandler(INoteRepository noteRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public GetByIdFolderNoteOutput handle(int id){
        Iterable<DbNote> dbNotes;
        dbNotes = noteRepository.findByFolderId(id);

        GetByIdFolderNoteOutput output = new GetByIdFolderNoteOutput();

        for (DbNote entity : dbNotes){
            output.notes.add(noteMapper.map(entity, GetByIdFolderNoteOutput.Note.class));
        }
        return output;
    }
}
