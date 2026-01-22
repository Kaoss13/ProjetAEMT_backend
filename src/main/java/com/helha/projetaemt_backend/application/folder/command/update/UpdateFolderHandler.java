package com.helha.projetaemt_backend.application.folder.command.update;

import com.helha.projetaemt_backend.application.utils.IEffectCommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UpdateFolderHandler implements IEffectCommandHandler<UpdateFolderInput> {
    private final IFolderRepository folderRepository;

    public UpdateFolderHandler(IFolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    @Override
    public void handle(final UpdateFolderInput input) {
        folderRepository
                .findById(input.id)
                .map(f -> {
                    if (f.getUser() == null || f.getUser().getId() != input.userId) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Ce dossier n'appartient pas à cet utilisateur"
                        );
                    }
                    String newTitle = input.title.trim();
                    if (f.title != null && f.title.equalsIgnoreCase(newTitle)) {
                        return f;
                    }
                    boolean exists;
                    if (f.getParentFolder() == null) {
                        exists = folderRepository
                                .existsByUser_IdAndParentFolderIsNullAndTitleIgnoreCase(input.userId, newTitle);
                    } else {
                        exists = folderRepository
                                .existsByUser_IdAndParentFolder_IdAndTitleIgnoreCase(
                                        input.userId,
                                        f.getParentFolder().id,
                                        newTitle
                                );
                    }
                    if (exists) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Le dossier existe déjà"
                        );
                    }

                    f.title = newTitle;
                    return folderRepository.save(f);
                }).orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Dossier introuvable"
                ));
    }
}
