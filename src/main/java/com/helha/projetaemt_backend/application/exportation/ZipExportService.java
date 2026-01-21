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



@Service
public class ZipExportService {

    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;

    public ZipExportService(IFolderRepository folderRepository, INoteRepository noteRepository) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
    }

    public byte[] exportFolderHierarchyToZip(DbFolder parentFolder) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(output);

        // Appel récursif pour ajouter le dossier et ses sous-dossiers
        addFolderToZip(zipOut, parentFolder, parentFolder.title);

        zipOut.close();
        return output.toByteArray();
    }

    private void addFolderToZip(ZipOutputStream zipOut, DbFolder folder, String path) throws IOException {
        List<DbNote> notes = noteRepository.findByFolderId(folder.id);

        List<DbFolder> subFolders = folderRepository.findByParentFolder_Id(folder.id);

        String folderInfo = "# Dossier : " + folder.title + "\n" +
                "Créé le : " + folder.createdAt + "\n" +
                "Nombre de sous-dossiers : " + subFolders.size() + "\n" +
                "Nombre de notes : " + notes.size() + "\n";

        zipOut.putNextEntry(new ZipEntry(path + "/README.md"));
        zipOut.write(folderInfo.getBytes(StandardCharsets.UTF_8));
        zipOut.closeEntry();

        for (DbNote note : notes) {
            String fileName = note.title.replaceAll("\\s+", "_") + ".md";
            String content = "# " + note.title + "\n\n" +
                    "Créé le : " + note.createdAt + "\n" +
                    "Dernière mise à jour : " + note.updatedAt + "\n\n" +
                    note.content;

            zipOut.putNextEntry(new ZipEntry(path + "/" + fileName));
            zipOut.write(content.getBytes(StandardCharsets.UTF_8));
            zipOut.closeEntry();
        }

        for (DbFolder subFolder : subFolders) {
            addFolderToZip(zipOut, subFolder, path + "/" + subFolder.title);
        }
    }
}

