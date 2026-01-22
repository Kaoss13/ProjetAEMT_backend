package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.domain.note.Note;
import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service responsible for exporting notes to a PDF document.
 * It generates a PDF containing a summary (table of contents) and detailed note information.
 */
@Service
public class PdfExportService {

    private final INoteRepository noteRepository;

    /**
     * Constructor injecting the note repository.
     *
     * @param noteRepository Repository used to fetch notes from the database.
     */
    public PdfExportService(INoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * Exports a list of notes to a PDF file.
     * The PDF includes a table of contents, metadata, and note content with internal links.
     *
     * @param notes      List of notes to export.
     * @param appBaseUrl Base URL of the application (used for external links).
     * @return Byte array representing the generated PDF.
     * @throws IOException if an error occurs during PDF generation.
     */
    public byte[] exportNotesToPdf(List<DbNote> notes, String appBaseUrl) throws IOException {
        Document doc = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, output);
        doc.open();

        // Define fonts for different sections of the PDF
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font linkFont = new Font(Font.HELVETICA, 12, Font.UNDERLINE, Color.BLUE);
        Font metaFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Collect all notes including mentioned notes
        Map<Integer, DbNote> allNotes = new LinkedHashMap<>();
        for (DbNote n : notes) {
            allNotes.put(n.id, n);
        }
        collectAllMentionedNotes(allNotes);

        // Add table of contents
        doc.add(new Paragraph("Summary:", titleFont));
        for (DbNote n : allNotes.values()) {
            Anchor tocLink = new Anchor(n.title, linkFont);
            tocLink.setReference("#note-" + n.id);
            doc.add(tocLink);
            doc.add(new Paragraph(" "));
        }
        doc.add(new Paragraph(" "));

        // Add detailed note content
        for (DbNote dbNote : allNotes.values()) {
            Anchor titleAnchor = new Anchor(dbNote.title, titleFont);
            titleAnchor.setName("note-" + dbNote.id);
            doc.add(titleAnchor);
            doc.add(new Paragraph(" "));

            // Add metadata
            doc.add(new Paragraph("Created on: " + dbNote.createdAt.format(formatter)));
            doc.add(new Paragraph("Last updated: " + dbNote.updatedAt.format(formatter)));

            String userName = dbNote.user != null ? dbNote.user.userName : "Unknown user";
            String folderName = dbNote.folder != null ? dbNote.folder.title : "No folder";
            doc.add(new Paragraph("Author: " + userName));
            doc.add(new Paragraph("Folder: " + folderName));

            // Compute note statistics
            Note noteDomain = new Note(dbNote.id,
                    dbNote.user != null ? dbNote.user.id : 0,
                    dbNote.folder != null ? dbNote.folder.getId() : 0,
                    dbNote.title,
                    dbNote.content != null ? dbNote.content : "");

            doc.add(new Paragraph("Size (bytes): " + noteDomain.computeSizeBytes(), metaFont));
            doc.add(new Paragraph("Line count: " + noteDomain.computeLineCount(), metaFont));
            doc.add(new Paragraph("Word count: " + noteDomain.computeWordCount(), metaFont));
            doc.add(new Paragraph("Character count: " + noteDomain.computeCharCount(), metaFont));
            doc.add(new Paragraph(" "));

            // Add note content with internal/external links
            Paragraph paragraph = new Paragraph();
            appendContentWithLinks(dbNote, allNotes, appBaseUrl, paragraph, linkFont);
            doc.add(paragraph);
            doc.add(new Paragraph(" "));
        }

        doc.close();
        return output.toByteArray();
    }

    /**
     * Collects all notes mentioned within the content of the given notes.
     * This ensures that referenced notes are also included in the PDF.
     *
     * @param allNotes Map of notes to be updated with mentioned notes.
     */
    private void collectAllMentionedNotes(Map<Integer, DbNote> allNotes) {
        boolean newNotesFound;
        do {
            newNotesFound = false;
            List<DbNote> currentNotes = new ArrayList<>(allNotes.values());

            for (DbNote note : currentNotes) {
                List<Integer> mentionedIds = extractMentionIds(note.content);
                for (Integer noteId : mentionedIds) {
                    if (!allNotes.containsKey(noteId)) {
                        Optional<DbNote> mentionedNoteOpt = noteRepository.findById(noteId);
                        if (mentionedNoteOpt.isPresent()) {
                            allNotes.put(noteId, mentionedNoteOpt.get());
                            newNotesFound = true;
                        }
                    }
                }
            }
        } while (newNotesFound);
    }

    /**
     * Extracts IDs of mentioned notes from HTML content.
     *
     * @param htmlContent HTML content containing mentions.
     * @return List of mentioned note IDs.
     */
    private List<Integer> extractMentionIds(String htmlContent) {
        List<Integer> ids = new ArrayList<>();
        if (htmlContent == null || htmlContent.isEmpty()) return ids;

        org.jsoup.nodes.Document doc = Jsoup.parse(htmlContent);
        Elements mentions = doc.select("span.note-mention");
        for (Element mention : mentions) {
            String noteIdStr = mention.attr("data-id");
            try {
                ids.add(Integer.parseInt(noteIdStr));
            } catch (NumberFormatException ignored) {
            }
        }
        return ids;
    }

    /**
     * Appends note content to the PDF paragraph, converting mentions into clickable links.
     *
     * @param note       Current note being processed.
     * @param allNotes   Map of all notes included in the PDF.
     * @param appBaseUrl Base URL for external links.
     * @param paragraph  Paragraph to append content to.
     * @param linkFont   Font used for links.
     */
    private void appendContentWithLinks(DbNote note, Map<Integer, DbNote> allNotes, String appBaseUrl,
                                        Paragraph paragraph, Font linkFont) {
        String content = note.content != null ? note.content : "";
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(content);
        Elements paragraphs = htmlDoc.body().select("p");

        for (Element p : paragraphs) {
            Paragraph subParagraph = new Paragraph();
            for (Node node : p.childNodes()) {
                if (node instanceof TextNode) {
                    // Add plain text
                    subParagraph.add(new Chunk(((TextNode) node).text() + " "));
                } else if (node instanceof Element) {
                    Element child = (Element) node;
                    // Handle note mentions
                    if (child.tagName().equals("span") && child.hasClass("note-mention")) {
                        String label = child.attr("data-label");
                        String noteIdStr = child.attr("data-id");
                        try {
                            int noteId = Integer.parseInt(noteIdStr);
                            Anchor link = new Anchor("@" + label, linkFont);
                            if (allNotes.containsKey(noteId)) {
                                link.setReference("#note-" + noteId);
                            } else {
                                link.setReference(appBaseUrl + "/note/" + noteId);
                            }
                            subParagraph.add(link);
                        } catch (NumberFormatException e) {
                            subParagraph.add(new Chunk("@" + label + " (Invalid ID) "));
                        }
                    }
                }
            }
            paragraph.add(subParagraph);
        }
    }
}
