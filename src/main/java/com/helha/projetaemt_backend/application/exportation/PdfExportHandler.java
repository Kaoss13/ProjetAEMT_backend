package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Handler responsible for managing the PDF export process.
 * It retrieves the main note and all mentioned notes, then delegates the PDF generation to PdfExportService.
 */
@Service
public class PdfExportHandler {

    private final INoteRepository noteRepository;
    private final PdfExportService pdfExportService;

    public PdfExportHandler(INoteRepository noteRepository, PdfExportService pdfExportService) {
        this.noteRepository = noteRepository;
        this.pdfExportService = pdfExportService;
    }

    /**
     * Handles the PDF export for a given note ID.
     * It retrieves the main note, extracts all mentioned notes, and generates a PDF containing them.
     *
     * @param noteId     ID of the main note to export.
     * @param appBaseUrl Base URL of the application (used for external links in the PDF).
     * @return Byte array representing the generated PDF.
     * @throws Exception if the note is not found or an error occurs during export.
     */
    public byte[] handle(int noteId, String appBaseUrl) throws Exception {
        // Fetch the main note or throw an exception if not found
        DbNote mainNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));

        // Parse the HTML content of the main note to find mentions
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(mainNote.content);
        Elements mentions = htmlDoc.select("span.note-mention");

        // Collect all note IDs: main note + mentioned notes
        List<Integer> allIds = new ArrayList<>();
        allIds.add(noteId);

        for (Element mention : mentions) {
            String mentionedId = mention.attr("data-id");
            if (mentionedId != null && !mentionedId.isEmpty()) {
                allIds.add(Integer.parseInt(mentionedId));
            }
        }

        // Retrieve all notes by their IDs
        List<DbNote> notes = StreamSupport.stream(noteRepository.findAllById(allIds).spliterator(), false)
                .collect(Collectors.toList());

        // Delegate PDF generation to PdfExportService
        return pdfExportService.exportNotesToPdf(notes, appBaseUrl);
    }
}
