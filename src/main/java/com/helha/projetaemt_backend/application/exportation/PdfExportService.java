package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.Color; // OK pour la couleur
import java.io.ByteArrayOutputStream;
import java.io.IOException;



@Service
public class PdfExportService {

    public byte[] exportNoteToPdf(DbNote note) throws IOException {
        Document doc = new Document();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PdfWriter.getInstance(doc, output);
        doc.open();

        // Titre
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        doc.add(new Paragraph(note.title, titleFont));

        // Création d'un objet métier pour calculer les métadonnées
        String content = note.content != null ? note.content : "";
        Note noteDomain = new Note();
        noteDomain.setContent(content);

        // Ajout des métadonnées calculées à la volée
        doc.add(new Paragraph("Créé le : " + note.createdAt));
        doc.add(new Paragraph("Dernière mise à jour : " + note.updatedAt));
        doc.add(new Paragraph("Taille : " + noteDomain.getSizeBytes() + " bytes"));
        doc.add(new Paragraph("Nombre de lignes : " + noteDomain.getLineCount()));
        doc.add(new Paragraph("Nombre de mots : " + noteDomain.getWordCount()));
        doc.add(new Paragraph("Nombre de caractères : " + noteDomain.getCharCount()));
        doc.add(new Paragraph(" "));
        doc.add(new Paragraph("Contenu :"));

        // Gestion des liens dans le contenu
        Paragraph paragraph = new Paragraph();
        Font linkFont = new Font(Font.HELVETICA, 12, Font.UNDERLINE, Color.BLUE);

        String[] words = content.split(" ");
        for (String word : words) {
            if (word.startsWith("http://") || word.startsWith("https://")) {
                Anchor link = new Anchor(word, linkFont);
                link.setReference(word); // rend le lien cliquable
                paragraph.add(link);
                paragraph.add(new Chunk(" ")); // espace après le lien
            } else {
                paragraph.add(new Chunk(word + " "));
            }
        }

        doc.add(paragraph);
        doc.close();

        return output.toByteArray();
    }
}
