package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.command.FolderCommandProcessor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/folders")
public class FolderCommandController {
    private final FolderCommandProcessor folderCommandProcessor;

    public FolderCommandController(FolderCommandProcessor folderCommandProcessor){
        this.folderCommandProcessor = folderCommandProcessor;
    }
}
