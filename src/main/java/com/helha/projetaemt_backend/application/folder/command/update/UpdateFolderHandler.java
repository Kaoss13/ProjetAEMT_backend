package com.helha.projetaemt_backend.application.folder.command.update;

import com.helha.projetaemt_backend.application.utils.IEffectCommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UpdateFolderHandler implements IEffectCommandHandler<UpdateFolderInput> {
    private final IFolderRepository folderRepository;

    public UpdateFolderHandler(IFolderRepository folderRepository){
        this.folderRepository = folderRepository;
    }

    //Vérifier que le nom de celui-ci n'existe pas au même niveau +VERIFIER SI DOSSIER PARENT OU NON POUR CELa
    @Override
    public void handle(final UpdateFolderInput input) {
        //Le titre est obligatoire
        if (input.title == null || input.title.trim().isEmpty()) {
            throw new IllegalArgumentException("The folder title is mandatory.");
        }

        folderRepository
                .findById(input.id)
                .map(f -> {
                    //Ici pas besoin de toLowerCase() car equalsIgnoreCase le fait déjà
                    String newTitle = input.title.trim();
                    if (f.title != null && f.title.equalsIgnoreCase(newTitle)) {
                        return f; // rien à faire car même title
                    }
                    //Gérer l'unicité des dossiers
                    boolean exists;
                    //Si celui-ci est un dossier parent
                    if (f.getParentFolder() == null) {
                        exists = folderRepository
                                .existsByUser_IdAndParentFolderIsNullAndTitleIgnoreCase(input.userId, newTitle);
                    }
                    //Si celui-ci est un sous dossier
                    else {
                        exists = folderRepository
                                .existsByUser_IdAndParentFolder_IdAndTitleIgnoreCase(
                                        input.userId,
                                        f.getParentFolder().id,
                                        newTitle
                                );
                    }
                    if (exists) {
                        throw new IllegalArgumentException("Folder already exists");
                    }
                    if (!folderRepository.existsByIdAndUser_Id(input.id, input.userId)) {
                        throw new IllegalArgumentException("Folder does not belong to this user");
                    }

                    f.title = newTitle;
                    return folderRepository.save(f);
                }).orElseThrow(() -> new IllegalArgumentException("Folder not found" + input.id));
    }
}
