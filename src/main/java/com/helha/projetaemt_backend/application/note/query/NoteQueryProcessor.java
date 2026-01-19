package com.helha.projetaemt_backend.application.note.query;

import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteOutput;
import org.springframework.stereotype.Service;

@Service
public class NoteQueryProcessor {
    public final GetByIdNoteHandler getByIdNoteHandler;
    public final GetByIdFolderNoteHandler getByIdFolderNoteHandler;

    public NoteQueryProcessor(GetByIdNoteHandler getByIdNoteHandler, GetByIdFolderNoteHandler getByIdFolderNoteHandler) {
        this.getByIdNoteHandler = getByIdNoteHandler;
        this.getByIdFolderNoteHandler = getByIdFolderNoteHandler;
    }
}
