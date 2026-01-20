package com.helha.projetaemt_backend.application.folder.command.delete;

import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.stereotype.Service;

@Service
public class DeleteFolderHandler {
    private final IFolderRepository folderRepository;

    public DeleteFolderHandler(IFolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    public void handle(int folderId){

        var folder = folderRepository
                .findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("Folder not found" + folderId));
        folderRepository.delete(folder);
    }
}
