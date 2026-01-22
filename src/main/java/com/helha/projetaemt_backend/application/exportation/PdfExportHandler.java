package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PdfExportHandler {
    private final INoteRepository noteRepository;
    private final PdfExportService pdfExportService;

    public PdfExportHandler(INoteRepository noteRepository, PdfExportService pdfExportService) {
        this.noteRepository = noteRepository;
        this.pdfExportService = pdfExportService;
    }

    public byte[] handle(int id ) throws Exception{
        DbNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));
        return pdfExportService.exportNoteToPdf(note);
    }
}
