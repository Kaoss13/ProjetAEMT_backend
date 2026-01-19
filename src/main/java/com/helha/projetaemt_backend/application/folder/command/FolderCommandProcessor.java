package com.helha.projetaemt_backend.application.folder.command;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderCommandProcessor {
    public final CreateFolderHandler createFolerHandler;

    public FolderCommandProcessor(CreateFolderHandler createFolerHandler){
        this.createFolerHandler = createFolerHandler;
    }
}
