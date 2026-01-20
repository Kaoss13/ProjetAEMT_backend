package com.helha.projetaemt_backend.application.folder.query;

import com.helha.projetaemt_backend.application.exportation.ZipExportHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    public final ZipExportHandler zipExportHandler;

    public FolderQueryProcessor(ZipExportHandler zipExportHandler) {
        this.zipExportHandler = zipExportHandler;
    }
}
