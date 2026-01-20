package com.helha.projetaemt_backend.application.folder.command;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderHandler;
import com.helha.projetaemt_backend.application.folder.command.delete.DeleteFolderHandler;
import com.helha.projetaemt_backend.application.folder.command.update.UpdateFolderHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderCommandProcessor {
    public final CreateFolderHandler createFolerHandler;
    public final UpdateFolderHandler updateFolderHandler;
    public final DeleteFolderHandler deleteFolderHandler;

    public FolderCommandProcessor(CreateFolderHandler createFolerHandler,
                                  UpdateFolderHandler updateFolderHandler,
                                  DeleteFolderHandler deleteFolderHandler){
        this.createFolerHandler = createFolerHandler;
        this.updateFolderHandler = updateFolderHandler;
        this.deleteFolderHandler = deleteFolderHandler;
    }
}
