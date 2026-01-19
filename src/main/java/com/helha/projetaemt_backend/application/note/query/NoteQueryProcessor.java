package com.helha.projetaemt_backend.application.note.query;

import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import org.springframework.stereotype.Service;

@Service
public class NoteQueryProcessor {
    public final GetByIdNoteHandler getByIdNoteHandler;

    public NoteQueryProcessor(GetByIdNoteHandler getByIdNoteHandler) {
        this.getByIdNoteHandler = getByIdNoteHandler;
    }
}
