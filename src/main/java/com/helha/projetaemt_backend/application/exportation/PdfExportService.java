package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.domain.note.Note;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
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
import java.util.List;

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

            // Add note content with formatting (bold, italic, tables, etc.)
            appendContentToDocument(doc, dbNote, allNotes, appBaseUrl, linkFont);
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
     * Appends note content to the PDF document, converting HTML to formatted PDF elements.
     * Handles tables separately since they need to be added directly to the document.
     */
    private void appendContentToDocument(Document doc, DbNote note, Map<Integer, DbNote> allNotes,
                                         String appBaseUrl, Font linkFont) throws DocumentException {
        String content = note.content != null ? note.content : "";
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(content);

        Paragraph currentParagraph = new Paragraph();

        // Process all child elements of body
        for (Node node : htmlDoc.body().childNodes()) {
            if (node instanceof Element && ((Element) node).tagName().equalsIgnoreCase("table")) {
                // Add current paragraph before table
                if (!currentParagraph.isEmpty()) {
                    doc.add(currentParagraph);
                    currentParagraph = new Paragraph();
                }
                // Add table directly to document
                PdfPTable table = createPdfTable((Element) node);
                if (table != null) {
                    doc.add(table);
                }
            } else {
                processNode(node, currentParagraph, allNotes, appBaseUrl, linkFont, Font.NORMAL);
            }
        }

        // Add remaining paragraph
        if (!currentParagraph.isEmpty()) {
            doc.add(currentParagraph);
        }
    }

    /**
     * Recursively processes HTML nodes and converts them to PDF elements.
     */
    private void processNode(Node node, Paragraph paragraph, Map<Integer, DbNote> allNotes,
                            String appBaseUrl, Font linkFont, int fontStyle) {
        if (node instanceof TextNode) {
            String text = ((TextNode) node).text();
            if (!text.trim().isEmpty()) {
                Font font = new Font(Font.HELVETICA, 12, fontStyle);
                paragraph.add(new Chunk(text, font));
            }
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tag = el.tagName().toLowerCase();

            switch (tag) {
                case "strong":
                case "b":
                    // Bold text
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle | Font.BOLD);
                    }
                    break;

                case "em":
                case "i":
                    // Italic text
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle | Font.ITALIC);
                    }
                    break;

                case "u":
                    // Underline text
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle | Font.UNDERLINE);
                    }
                    break;

                case "s":
                case "strike":
                case "del":
                    // Strikethrough text
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle | Font.STRIKETHRU);
                    }
                    break;

                case "h1":
                    paragraph.add(Chunk.NEWLINE);
                    Font h1Font = new Font(Font.HELVETICA, 20, Font.BOLD);
                    paragraph.add(new Chunk(el.text(), h1Font));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "h2":
                    paragraph.add(Chunk.NEWLINE);
                    Font h2Font = new Font(Font.HELVETICA, 16, Font.BOLD);
                    paragraph.add(new Chunk(el.text(), h2Font));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "h3":
                    paragraph.add(Chunk.NEWLINE);
                    Font h3Font = new Font(Font.HELVETICA, 14, Font.BOLD);
                    paragraph.add(new Chunk(el.text(), h3Font));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "p":
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle);
                    }
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "br":
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "ul":
                    paragraph.add(Chunk.NEWLINE);
                    for (Element li : el.select("> li")) {
                        paragraph.add(new Chunk("  • " + li.text()));
                        paragraph.add(Chunk.NEWLINE);
                    }
                    break;

                case "ol":
                    paragraph.add(Chunk.NEWLINE);
                    int index = 1;
                    for (Element li : el.select("> li")) {
                        paragraph.add(new Chunk("  " + index + ". " + li.text()));
                        paragraph.add(Chunk.NEWLINE);
                        index++;
                    }
                    break;

                case "blockquote":
                    paragraph.add(Chunk.NEWLINE);
                    Font quoteFont = new Font(Font.HELVETICA, 12, Font.ITALIC, Color.DARK_GRAY);
                    paragraph.add(new Chunk("    \"" + el.text() + "\"", quoteFont));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "code":
                    Font codeFont = new Font(Font.COURIER, 11, fontStyle, Color.DARK_GRAY);
                    paragraph.add(new Chunk(el.text(), codeFont));
                    break;

                case "pre":
                    paragraph.add(Chunk.NEWLINE);
                    Font preFont = new Font(Font.COURIER, 10, Font.NORMAL, Color.DARK_GRAY);
                    paragraph.add(new Chunk(el.text(), preFont));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                case "span":
                    // Handle note mentions
                    if (el.hasClass("note-mention")) {
                        String label = el.attr("data-label");
                        String noteIdStr = el.attr("data-note-id");
                        if (noteIdStr.isEmpty()) {
                            noteIdStr = el.attr("data-id");
                        }
                        try {
                            int noteId = Integer.parseInt(noteIdStr);
                            Anchor link = new Anchor("@" + label, linkFont);
                            if (allNotes.containsKey(noteId)) {
                                link.setReference("#note-" + noteId);
                            } else {
                                link.setReference(appBaseUrl + "/note/" + noteId);
                            }
                            paragraph.add(link);
                        } catch (NumberFormatException e) {
                            paragraph.add(new Chunk("@" + label));
                        }
                    } else {
                        for (Node child : el.childNodes()) {
                            processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle);
                        }
                    }
                    break;

                case "a":
                    String href = el.attr("href");
                    Anchor anchor = new Anchor(el.text(), linkFont);
                    anchor.setReference(href);
                    paragraph.add(anchor);
                    break;

                case "hr":
                    paragraph.add(Chunk.NEWLINE);
                    paragraph.add(new Chunk("─────────────────────────────────"));
                    paragraph.add(Chunk.NEWLINE);
                    break;

                default:
                    // For other elements, process children
                    for (Node child : el.childNodes()) {
                        processNode(child, paragraph, allNotes, appBaseUrl, linkFont, fontStyle);
                    }
            }
        }
    }

    /**
     * Creates a PDF table from an HTML table element.
     */
    private PdfPTable createPdfTable(Element tableEl) throws DocumentException {
        Elements rows = tableEl.select("tr");
        if (rows.isEmpty()) return null;

        // Determine number of columns from first row
        Element firstRow = rows.first();
        int numCols = firstRow.select("th, td").size();
        if (numCols == 0) return null;

        PdfPTable table = new PdfPTable(numCols);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(10);

        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD);
        Font cellFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            for (Element cell : cells) {
                boolean isHeader = cell.tagName().equals("th");
                Font font = isHeader ? headerFont : cellFont;

                PdfPCell pdfCell = new PdfPCell(new Phrase(cell.text(), font));
                pdfCell.setPadding(5);
                if (isHeader) {
                    pdfCell.setBackgroundColor(new Color(240, 240, 240));
                }
                table.addCell(pdfCell);
            }
        }

        return table;
    }
}
