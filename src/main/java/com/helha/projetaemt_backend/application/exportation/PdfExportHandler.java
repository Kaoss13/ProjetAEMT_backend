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

@Service
public class PdfExportHandler {
    private final INoteRepository noteRepository;
    private final PdfExportService pdfExportService;

    public PdfExportHandler(INoteRepository noteRepository, PdfExportService pdfExportService) {
        this.noteRepository = noteRepository;
        this.pdfExportService = pdfExportService;
    }

    public byte[] handle(int noteId, String appBaseUrl) throws Exception {
        DbNote mainNote = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Note not found"
                ));

        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(mainNote.content);
        Elements mentions = htmlDoc.select("span.note-mention");

        List<Integer> allIds = new ArrayList<>();
        allIds.add(noteId);

        for (Element mention : mentions) {
            String mentionedId = mention.attr("data-id");
            if (mentionedId != null && !mentionedId.isEmpty()) {
                allIds.add(Integer.parseInt(mentionedId));
            }
        }

        List<DbNote> notes = StreamSupport.stream(noteRepository.findAllById(allIds).spliterator(), false)
                .collect(Collectors.toList());

        return pdfExportService.exportNotesToPdf(notes, appBaseUrl);
    }

}
