package com.helha.projetaemt_backend.application.note.command;

import com.helha.projetaemt_backend.application.note.command.create.CreateNoteHandler;
import com.helha.projetaemt_backend.application.note.command.delete.DeleteNoteHandler;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteHandler;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteInput;
import org.springframework.stereotype.Service;

@Service
public class NoteCommandProcessor {
    public final CreateNoteHandler createNoteHandler;
    public final UpdateNoteHandler updateNoteHandler;
    public final DeleteNoteHandler deleteNoteHandler;

    public NoteCommandProcessor(CreateNoteHandler createNoteHandler, UpdateNoteHandler updateNoteHandler, DeleteNoteHandler deleteNoteHandler) {
        this.createNoteHandler = createNoteHandler;
        this.updateNoteHandler = updateNoteHandler;
        this.deleteNoteHandler = deleteNoteHandler;
    }
}
