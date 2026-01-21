package com.helha.projetaemt_backend.application.exportation;
import com.helha.projetaemt_backend.application.note.exceptions.NoteNotFoundException;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class PdfExportHandler {

    private final INoteRepository noteRepository;
    private final PdfExportService pdfExportService;

    public PdfExportHandler(INoteRepository noteRepository, PdfExportService pdfExportService) {
        this.noteRepository = noteRepository;
        this.pdfExportService = pdfExportService;
    }


    public byte[] handle(List<Integer> noteIds, String appBaseUrl) throws Exception {
        List<DbNote> notes = StreamSupport.stream(noteRepository.findAllById(noteIds).spliterator(), false)
                .collect(Collectors.toList());

        if (notes.isEmpty()) {
            throw new RuntimeException("Aucune note trouvée pour les IDs : " + noteIds);
        }
        return pdfExportService.exportNotesToPdf(notes, appBaseUrl);
    }

    public byte[] handle(int noteId, String appBaseUrl) throws Exception {
        return handle(List.of(noteId), appBaseUrl);
    }

}

