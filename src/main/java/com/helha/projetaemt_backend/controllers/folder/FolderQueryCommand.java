package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.query.FolderQueryProcessor;
import com.helha.projetaemt_backend.application.folder.query.getAll.GetAllFoldersWithNotesOutput;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
public class FolderQueryCommand {
    private final FolderQueryProcessor folderQueryProcessor;

    public FolderQueryCommand(FolderQueryProcessor folderQueryProcessor){
        this.folderQueryProcessor = folderQueryProcessor;
    }


    @GetMapping("/all/{userId}")
    public ResponseEntity<GetAllFoldersWithNotesOutput> getAllFoldersWithNotes(@PathVariable int userId) {
        return ResponseEntity.ok(folderQueryProcessor.getAllFoldersWithNotesHandler.handle(userId));
    }
}
