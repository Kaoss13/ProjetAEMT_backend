package com.helha.projetaemt_backend.application.note.command.delete;

import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteNoteHandler {
    private INoteRepository noteRepository;

    public DeleteNoteHandler(INoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void handle(int id) {
        noteRepository.findById(id)
                .map(n -> {
                    noteRepository.delete(n);
                    return n;
                })
                .orElseThrow(() -> new IllegalArgumentException("Note not found " + id));
    }
}
