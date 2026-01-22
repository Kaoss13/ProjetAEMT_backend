package com.helha.projetaemt_backend.application.note.query.getbyidfolder;

import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GetByIdFolderNoteHandler {
    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;
    private final NoteMapper noteMapper;

    public GetByIdFolderNoteHandler(IFolderRepository folderRepository,
                                    INoteRepository noteRepository, NoteMapper noteMapper) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
    }

    public GetByIdFolderNoteOutput handle(int id){
        if (!folderRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Note introuvable"
            );
        }

        Iterable<DbNote> dbNotes;
        dbNotes = noteRepository.findByFolderId(id);

        GetByIdFolderNoteOutput output = new GetByIdFolderNoteOutput();

        for (DbNote entity : dbNotes){
            output.notes.add(noteMapper.map(entity, GetByIdFolderNoteOutput.Note.class));
        }
        return output;
    }
}
