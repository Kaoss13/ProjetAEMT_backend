package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Folder not found"
                ));
        return zipExportService.exportFolderHierarchyToZip(folder);
    }
}
