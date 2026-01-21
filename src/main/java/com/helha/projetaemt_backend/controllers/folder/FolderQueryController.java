package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.query.FolderQueryProcessor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
public class FolderQueryController {
    private final FolderQueryProcessor folderQueryProcessor;

    public FolderQueryController(FolderQueryProcessor folderQueryProcessor) {
        this.folderQueryProcessor = folderQueryProcessor;
    }


    @GetMapping("/{id}/export-zip")
    public ResponseEntity<byte[]> exportFolderToZip(@PathVariable int id) throws Exception {

        byte[] zipBytes = folderQueryProcessor.zipExportHandler.handle(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=folder_" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }


}
