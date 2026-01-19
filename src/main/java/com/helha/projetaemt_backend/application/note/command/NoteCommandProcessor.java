package com.helha.projetaemt_backend.application.note.command;

import com.helha.projetaemt_backend.application.note.command.create.CreateNoteHandler;
import org.springframework.stereotype.Service;

@Service
public class NoteCommandProcessor {
    public final CreateNoteHandler createNoteHandler;

    public NoteCommandProcessor(CreateNoteHandler createNoteHandler) {
        this.createNoteHandler = createNoteHandler;
    }
}
