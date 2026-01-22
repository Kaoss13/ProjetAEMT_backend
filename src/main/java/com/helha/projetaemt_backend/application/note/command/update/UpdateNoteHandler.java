package com.helha.projetaemt_backend.application.note.command.update;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;


@Service
public class UpdateNoteHandler {
    private final INoteRepository noteRepository;
    private final ModelMapper modelMapper;

    public UpdateNoteHandler(INoteRepository noteRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.modelMapper = modelMapper;
    }

    public UpdateNoteOutput handle(UpdateNoteInput input) {
        DbNote updated = noteRepository.findById(input.id)
                .map(n -> {
                    if (input.title != null) {
                        n.title = input.title;
                    }
                    if (input.content != null) {
                        n.content = input.content;
                    }
                    n.updatedAt = LocalDateTime.now();
                    return noteRepository.save(n);
                })
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note introuvable"
                ));

        // === Calcul des métadonnées à la volée ===
        Note noteDomain = new Note();
        noteDomain.setContent(updated.content != null ? updated.content : "");

        // === Mapping vers DTO enrichi ===
        UpdateNoteOutput output = modelMapper.map(updated, UpdateNoteOutput.class);
        output.sizeBytes = noteDomain.computeSizeBytes();
        output.lineCount = noteDomain.computeLineCount();
        output.wordCount = noteDomain.computeWordCount();
        output.charCount = noteDomain.computeCharCount();

        return output;
    }
}


