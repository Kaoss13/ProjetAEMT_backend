package com.helha.projetaemt_backend.application.note.command.update;

import com.helha.projetaemt_backend.NoteInputMapper;
import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Service
public class UpdateNoteHandler {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;
    private final NoteInputMapper noteInputMapper;
    private final NoteMapper noteMapper;

    public UpdateNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper, NoteInputMapper noteInputMapper, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
        this.noteInputMapper = noteInputMapper;
        this.noteMapper = noteMapper;
    }

    public void handle(UpdateNoteInput input) {
        noteRepository
                .findById(input.id)
                .map( n -> {
                    if (input.title != null)
                        n.title = input.title;
                    if (input.content != null){
                        n.content = input.content;
                        n.charCount = (input.content.length());
                        n.sizeBytes = (input.content.getBytes(StandardCharsets.UTF_8).length);
                        n.lineCount = (input.content.isEmpty() ? 0 : input.content.split("\r\n|\r|\n").length);
                        n.wordCount = (input.content.trim().isEmpty() ? 0 : input.content.trim().split("\\s+").length);
                    }
                    n.updatedAt = LocalDateTime.now();

                    return noteRepository.save(n);
                }).orElseThrow(() -> new IllegalArgumentException("Note not found " + input.id));
    }
}
