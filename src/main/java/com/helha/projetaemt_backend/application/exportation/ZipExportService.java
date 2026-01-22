package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Service responsible for exporting a folder hierarchy into a ZIP archive.
 * Each folder and its notes are added recursively, including metadata files.
 */
@Service
public class ZipExportService {

    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;

    /**
     * Constructor injecting dependencies.
     *
     * @param folderRepository Repository for accessing folders from the database.
     * @param noteRepository   Repository for accessing notes from the database.
     */
    public ZipExportService(IFolderRepository folderRepository, INoteRepository noteRepository) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
    }

    /**
     * Exports the entire hierarchy of a given folder into a ZIP archive.
     *
     * @param parentFolder The root folder to export.
     * @return Byte array representing the generated ZIP archive.
     * @throws IOException if an error occurs during ZIP creation.
     */
    public byte[] exportFolderHierarchyToZip(DbFolder parentFolder) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(output);

        // Add the root folder and its contents recursively
        addFolderToZip(zipOut, parentFolder, parentFolder.title);

        zipOut.close();
        return output.toByteArray();
    }

    /**
     * Adds a folder and its contents (notes and subfolders) to the ZIP archive.
     * Creates a README.md file with folder metadata and adds each note as a Markdown file.
     *
     * @param zipOut ZIP output stream.
     * @param folder Current folder to process.
     * @param path   Path inside the ZIP archive representing the folder hierarchy.
     * @throws IOException if an error occurs during writing.
     */
    private void addFolderToZip(ZipOutputStream zipOut, DbFolder folder, String path) throws IOException {
        // Retrieve notes in the current folder
        List<DbNote> notes = noteRepository.findByFolderId(folder.id);

        // Retrieve subfolders of the current folder
        List<DbFolder> subFolders = folderRepository.findByParentFolder_Id(folder.id);

        // Create folder metadata (README.md)
        String folderInfo = "# Folder: " + folder.title + "\n" +
                "Created on: " + folder.createdAt + "\n" +
                "Number of subfolders: " + subFolders.size() + "\n" +
                "Number of notes: " + notes.size() + "\n";

        zipOut.putNextEntry(new ZipEntry(path + "/README.md"));
        zipOut.write(folderInfo.getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();

        // Add each note as a Markdown file
        for (DbNote note : notes) {
            String fileName = note.title.replaceAll("\\s+", "_") + ".md";
            String markdownContent = convertHtmlToMarkdown(note.content);

            // Compute metadata using Note domain
            Note noteDomain = new Note();
            noteDomain.setContent(note.content != null ? note.content : "");

            String author = note.user != null ? note.user.userName : "Inconnu";
            String folderName = note.folder != null ? note.folder.title : "Racine";

            String content = "# " + note.title + "\n\n" +
                    "---\n" +
                    "**Auteur:** " + author + "\n" +
                    "**Dossier:** " + folderName + "\n" +
                    "**Créé le:** " + note.createdAt + "\n" +
                    "**Modifié le:** " + note.updatedAt + "\n" +
                    "**Taille:** " + noteDomain.computeSizeBytes() + " octets\n" +
                    "**Lignes:** " + noteDomain.computeLineCount() + "\n" +
                    "**Mots:** " + noteDomain.computeWordCount() + "\n" +
                    "**Caractères:** " + noteDomain.computeCharCount() + "\n" +
                    "---\n\n" +
                    markdownContent;

            zipOut.putNextEntry(new ZipEntry(path + "/" + fileName));
            zipOut.write(content.getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        }

        // Recursively add subfolders
        for (DbFolder subFolder : subFolders) {
            addFolderToZip(zipOut, subFolder, path + "/" + subFolder.title);
        }
    }

    /**
     * Converts HTML content to Markdown format.
     */
    private String convertHtmlToMarkdown(String html) {
        if (html == null || html.isEmpty()) {
            return "";
        }

        Document doc = Jsoup.parse(html);
        StringBuilder markdown = new StringBuilder();

        for (Node node : doc.body().childNodes()) {
            processNodeToMarkdown(node, markdown);
        }

        return markdown.toString().trim();
    }

    /**
     * Recursively processes HTML nodes and converts them to Markdown.
     */
    private void processNodeToMarkdown(Node node, StringBuilder sb) {
        if (node instanceof TextNode) {
            sb.append(((TextNode) node).text());
        } else if (node instanceof Element) {
            Element el = (Element) node;
            String tag = el.tagName().toLowerCase();

            switch (tag) {
                case "strong":
                case "b":
                    sb.append("**");
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    sb.append("**");
                    break;

                case "em":
                case "i":
                    sb.append("*");
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    sb.append("*");
                    break;

                case "u":
                    sb.append("<u>");
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    sb.append("</u>");
                    break;

                case "s":
                case "strike":
                case "del":
                    sb.append("~~");
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    sb.append("~~");
                    break;

                case "h1":
                    sb.append("\n# ").append(el.text()).append("\n\n");
                    break;

                case "h2":
                    sb.append("\n## ").append(el.text()).append("\n\n");
                    break;

                case "h3":
                    sb.append("\n### ").append(el.text()).append("\n\n");
                    break;

                case "p":
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    sb.append("\n\n");
                    break;

                case "br":
                    sb.append("\n");
                    break;

                case "ul":
                    sb.append("\n");
                    for (Element li : el.select("> li")) {
                        sb.append("- ");
                        for (Node child : li.childNodes()) processNodeToMarkdown(child, sb);
                        sb.append("\n");
                    }
                    sb.append("\n");
                    break;

                case "ol":
                    sb.append("\n");
                    int idx = 1;
                    for (Element li : el.select("> li")) {
                        sb.append(idx++).append(". ");
                        for (Node child : li.childNodes()) processNodeToMarkdown(child, sb);
                        sb.append("\n");
                    }
                    sb.append("\n");
                    break;

                case "blockquote":
                    sb.append("\n> ").append(el.text().replace("\n", "\n> ")).append("\n\n");
                    break;

                case "code":
                    sb.append("`").append(el.text()).append("`");
                    break;

                case "pre":
                    sb.append("\n```\n").append(el.text()).append("\n```\n\n");
                    break;

                case "a":
                    String href = el.attr("href");
                    sb.append("[").append(el.text()).append("](").append(href).append(")");
                    break;

                case "hr":
                    sb.append("\n---\n\n");
                    break;

                case "table":
                    sb.append("\n");
                    convertTableToMarkdown(el, sb);
                    sb.append("\n");
                    break;

                case "span":
                    // Handle mentions
                    if (el.hasClass("note-mention")) {
                        String label = el.attr("data-label");
                        if (label.isEmpty()) label = el.text();
                        sb.append("@").append(label);
                    } else {
                        for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
                    }
                    break;

                default:
                    for (Node child : el.childNodes()) processNodeToMarkdown(child, sb);
            }
        }
    }

    /**
     * Converts an HTML table to Markdown table format.
     */
    private void convertTableToMarkdown(Element table, StringBuilder sb) {
        Elements rows = table.select("tr");
        if (rows.isEmpty()) return;

        boolean isFirstRow = true;
        for (Element row : rows) {
            Elements cells = row.select("th, td");
            sb.append("|");
            for (Element cell : cells) {
                sb.append(" ").append(cell.text()).append(" |");
            }
            sb.append("\n");

            // Add separator after header row
            if (isFirstRow) {
                sb.append("|");
                for (int i = 0; i < cells.size(); i++) {
                    sb.append(" --- |");
                }
                sb.append("\n");
                isFirstRow = false;
            }
        }
    }
}
