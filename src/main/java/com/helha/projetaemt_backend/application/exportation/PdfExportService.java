package com.helha.projetaemt_backend.application.exportation;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.lowagie.text.Anchor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document; // iText Document
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    public byte[] exportNotesToPdf(List<DbNote> notes, String appBaseUrl) throws IOException {
        Document doc = new Document(); // iText Document
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, output);
        doc.open();

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font linkFont = new Font(Font.HELVETICA, 12, Font.UNDERLINE, Color.BLUE);

        // ✅ Map des notes incluses
        Map<Integer, DbNote> notesIncluses = new HashMap<>();
        for (DbNote n : notes) {
            notesIncluses.put(n.id, n);
        }

        // ✅ Sommaire
        doc.add(new Paragraph("Sommaire :", titleFont));
        for (DbNote n : notes) {
            Anchor tocLink = new Anchor(n.title, linkFont);
            tocLink.setReference("#note-" + n.id);
            doc.add(tocLink);
            doc.add(new Paragraph(" "));
        }
        doc.add(new Paragraph(" "));

        // ✅ Ajout des notes
        for (DbNote note : notes) {
            Anchor titleAnchor = new Anchor(note.title, titleFont);
            titleAnchor.setName("note-" + note.id);
            doc.add(titleAnchor);
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("Créé le : " + note.createdAt));
            doc.add(new Paragraph("Dernière mise à jour : " + note.updatedAt));
            doc.add(new Paragraph(" "));

            String content = note.content != null ? note.content : "";

            // ✅ Utilisation explicite de Jsoup Document
            org.jsoup.nodes.Document htmlDoc = Jsoup.parse(content);
            Paragraph paragraph = new Paragraph();

            Elements elements = htmlDoc.body().children();
            for (Element el : elements) {
                if (el.tagName().equals("span") && el.hasClass("note-mention")) {
                    String label = el.attr("data-label");
                    String noteIdStr = el.attr("data-id");
                    int noteId = Integer.parseInt(noteIdStr);

                    if (notesIncluses.containsKey(noteId)) {
                        Anchor internalLink = new Anchor("@" + label, linkFont);
                        internalLink.setReference("#note-" + noteId);
                        paragraph.add(internalLink);
                    } else {
                        Anchor externalLink = new Anchor("@" + label, linkFont);
                        externalLink.setReference(appBaseUrl + "/note/" + noteId);
                        paragraph.add(externalLink);
                    }
                } else {
                    paragraph.add(new Chunk(el.text() + " "));
                }
            }

            doc.add(paragraph);
            doc.add(new Paragraph(" "));
        }

        doc.close();
        return output.toByteArray();
    }
}
