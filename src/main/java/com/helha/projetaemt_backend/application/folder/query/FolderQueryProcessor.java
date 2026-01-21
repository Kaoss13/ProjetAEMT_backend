package com.helha.projetaemt_backend.application.folder.query;

import com.helha.projetaemt_backend.application.exportation.PdfExportHandler;
import com.helha.projetaemt_backend.application.exportation.ZipExportHandler;
import com.helha.projetaemt_backend.application.folder.query.getAll.GetAllFoldersWithNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    private final PdfExportHandler pdfExportHandler;
    public final ZipExportHandler zipExportHandler;
    public final GetAllFoldersWithNotesHandler getAllFoldersWithNotesHandler;

    public FolderQueryProcessor(PdfExportHandler pdfExportHandler, ZipExportHandler zipExportHandler, GetAllFoldersWithNotesHandler getAllFoldersWithNotesHandler) {
        this.pdfExportHandler = pdfExportHandler;
        this.zipExportHandler = zipExportHandler;
        this.getAllFoldersWithNotesHandler = getAllFoldersWithNotesHandler;
    }
}
