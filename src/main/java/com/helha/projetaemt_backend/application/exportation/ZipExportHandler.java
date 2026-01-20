package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ZipExportHandler {

    private final IFolderRepository folderRepository;
    private final ZipExportService zipExportService;

    public ZipExportHandler(IFolderRepository folderRepository, ZipExportService zipExportService) {
        this.folderRepository = folderRepository;
        this.zipExportService = zipExportService;
    }

    public byte[] handle(int folderId) throws Exception {
        DbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable avec l'ID : " + folderId));

        return zipExportService.exportFolderHierarchyToZip(folder);
    }
}
