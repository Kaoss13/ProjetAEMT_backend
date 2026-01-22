package com.helha.projetaemt_backend.application.folder.command.delete;

import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DeleteFolderHandler {
    private final IFolderRepository folderRepository;

    public DeleteFolderHandler(IFolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    public void handle(int folderId){

        var folder = folderRepository
                .findById(folderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Dossier introuvable."));
        folderRepository.delete(folder);
    }
}
