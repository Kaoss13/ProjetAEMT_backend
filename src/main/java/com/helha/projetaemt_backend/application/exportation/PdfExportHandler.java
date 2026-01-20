package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.application.note.exceptions.NoteNotFoundException;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Note introuvable avec l'ID : " + id));
        return pdfExportService.exportNoteToPdf(note);
    }
}
