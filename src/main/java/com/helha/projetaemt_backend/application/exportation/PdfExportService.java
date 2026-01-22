
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

@Service
public class PdfExportService {

    private final INoteRepository noteRepository;

    public PdfExportService(INoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public byte[] exportNotesToPdf(List<DbNote> notes, String appBaseUrl) throws IOException {
        Document doc = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, output);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font linkFont = new Font(Font.HELVETICA, 12, Font.UNDERLINE, Color.BLUE);
        Font metaFont = new Font(Font.HELVETICA, 10, Font.ITALIC, Color.DARK_GRAY);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Map<Integer, DbNote> allNotes = new LinkedHashMap<>();
        for (DbNote n : notes) {
            allNotes.put(n.id, n);
        }

        collectAllMentionedNotes(allNotes);

        doc.add(new Paragraph("Sommaire :", titleFont));
        for (DbNote n : allNotes.values()) {
            Anchor tocLink = new Anchor(n.title, linkFont);
            tocLink.setReference("#note-" + n.id);
            doc.add(tocLink);
            doc.add(new Paragraph(" "));
        }
        doc.add(new Paragraph(" "));

        for (DbNote dbNote : allNotes.values()) {
            Anchor titleAnchor = new Anchor(dbNote.title, titleFont);
            titleAnchor.setName("note-" + dbNote.id);
            doc.add(titleAnchor);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Créé le : " + dbNote.createdAt.format(formatter)));
            doc.add(new Paragraph("Dernière mise à jour : " + dbNote.updatedAt.format(formatter)));

            String userName = dbNote.user != null ? dbNote.user.userName : "Utilisateur inconnu";
            String folderName = dbNote.folder != null ? dbNote.folder.title : "Sans dossier";
            doc.add(new Paragraph("Auteur : " + userName));
            doc.add(new Paragraph("Dossier : " + folderName));

            Note noteDomain = new Note(dbNote.id,
                    dbNote.user != null ? dbNote.user.id : 0,
                    dbNote.folder != null ? dbNote.folder.getId() : 0,
                    dbNote.title,
                    dbNote.content != null ? dbNote.content : "");

            doc.add(new Paragraph("Taille (octets) : " + noteDomain.computeSizeBytes(), metaFont));
            doc.add(new Paragraph("Nombre de lignes : " + noteDomain.computeLineCount(), metaFont));
            doc.add(new Paragraph("Nombre de mots : " + noteDomain.computeWordCount(), metaFont));
            doc.add(new Paragraph("Nombre de caractères : " + noteDomain.computeCharCount(), metaFont));
            doc.add(new Paragraph(" "));

            Paragraph paragraph = new Paragraph();
            appendContentWithLinks(dbNote, allNotes, appBaseUrl, paragraph, linkFont);
            doc.add(paragraph);
            doc.add(new Paragraph(" "));
        }

        doc.close();
        return output.toByteArray();
    }

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

    private void appendContentWithLinks(DbNote note, Map<Integer, DbNote> allNotes, String appBaseUrl,
                                        Paragraph paragraph, Font linkFont) {
        String content = note.content != null ? note.content : "";
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(content);
        Elements paragraphs = htmlDoc.body().select("p");

        for (Element p : paragraphs) {
            Paragraph subParagraph = new Paragraph();
            for (Node node : p.childNodes()) {
                if (node instanceof TextNode) {
                    subParagraph.add(new Chunk(((TextNode) node).text() + " "));
                } else if (node instanceof Element) {
                    Element child = (Element) node;
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
                            subParagraph.add(new Chunk("@" + label + " (ID invalide) "));
                        }
                    }
                }
            }
            paragraph.add(subParagraph);
        }
    }
}
