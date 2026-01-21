package com.helha.projetaemt_backend.application.folder.command;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderHandler;
import com.helha.projetaemt_backend.application.folder.command.delete.DeleteFolderHandler;
import com.helha.projetaemt_backend.application.folder.command.update.UpdateFolderHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderCommandProcessor {
    public final CreateFolderHandler createFolderHandler;
    public final UpdateFolderHandler updateFolderHandler;
    public final DeleteFolderHandler deleteFolderHandler;

    public FolderCommandProcessor(CreateFolderHandler createFolderHandler,
                                  UpdateFolderHandler updateFolderHandler,
                                  DeleteFolderHandler deleteFolderHandler){
        this.createFolderHandler = createFolderHandler;
        this.updateFolderHandler = updateFolderHandler;
        this.deleteFolderHandler = deleteFolderHandler;
    }
}
