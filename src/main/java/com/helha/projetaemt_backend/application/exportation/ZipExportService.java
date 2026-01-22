package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
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
            String content = "# " + note.title + "\n\n" +
                    "Created on: " + note.createdAt + "\n" +
                    "Last updated: " + note.updatedAt + "\n\n" +
                    note.content;

            zipOut.putNextEntry(new ZipEntry(path + "/" + fileName));
            zipOut.write(content.getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        }

        // Recursively add subfolders
        for (DbFolder subFolder : subFolders) {
            addFolderToZip(zipOut, subFolder, path + "/" + subFolder.title);
        }
    }
}
