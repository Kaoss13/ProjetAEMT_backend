package com.helha.projetaemt_backend.application.exportation;

import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


/**
 * Handler responsible for managing the ZIP export process.
 * It retrieves a folder and delegates the ZIP generation to ZipExportService.
 */
@Service
public class ZipExportHandler {

    private final IFolderRepository folderRepository;
    private final ZipExportService zipExportService;

    /**
     * Constructor injecting dependencies.
     *
     * @param folderRepository Repository for accessing folders from the database.
     * @param zipExportService Service responsible for generating the ZIP archive.
     */
    public ZipExportHandler(IFolderRepository folderRepository, ZipExportService zipExportService) {
        this.folderRepository = folderRepository;
        this.zipExportService = zipExportService;
    }

    /**
     * Handles the ZIP export for a given folder ID.
     * It retrieves the folder and generates a ZIP archive containing its hierarchy.
     *
     * @param folderId ID of the folder to export.
     * @return Byte array representing the generated ZIP archive.
     * @throws Exception if the folder is not found or an error occurs during export.
     */
    public byte[] handle(int folderId) throws Exception {
        // Fetch the folder or throw an exception if not found
        DbFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Folder not found"
                ));

        // Delegate ZIP generation to ZipExportService
        return zipExportService.exportFolderHierarchyToZip(folder);
    }
}
