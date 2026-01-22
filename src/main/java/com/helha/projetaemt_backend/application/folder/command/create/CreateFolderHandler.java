package com.helha.projetaemt_backend.application.folder.command.create;

import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import com.helha.projetaemt_backend.mapping.folder.CreateFolderInputMapper;
import com.helha.projetaemt_backend.mapping.folder.CreateFolderOutputMapper;
import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CreateFolderHandler implements ICommandHandler<CreateFolderInput, CreateFolderOutput> {
    private final IFolderRepository folderRepository;
    private final IUserRepository userRepository;
    private final CreateFolderInputMapper createFolderInputMapper;
    private final CreateFolderOutputMapper createFolderOutputMapper;

    public CreateFolderHandler(IFolderRepository folderRepository,
                               IUserRepository userRepository,
                               CreateFolderInputMapper createFolderInputMapper,
                               CreateFolderOutputMapper createFolderOutputMapper){
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.createFolderInputMapper = createFolderInputMapper;
        this.createFolderOutputMapper = createFolderOutputMapper;
    }


    @Override
    public CreateFolderOutput handle(CreateFolderInput input) {

        //Ici on fournit le statut + le message
        DbUser user = userRepository.findById(input.userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User introuvable"
                ));


        DbFolder parentFolder;
        if (input.parentFolderId == null || input.parentFolderId == 0) {
            parentFolder = folderRepository.findByUser_IdAndParentFolderIsNull(input.userId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Dossier racine introuvable pour cet utilisateur."
                    ));
        }
        else {
            parentFolder = folderRepository.findById(input.parentFolderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Dossier parent introuvable"
                    ));

            if (parentFolder.user.id != input.userId) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Le dossier parent n'appartient pas à cet utilisateur."
                );
            }
        }


        String normalizedTitle = input.title.trim();
        boolean alreadyExists =
                folderRepository.existsByUser_IdAndParentFolder_IdAndTitleIgnoreCase(
                        input.userId,
                        parentFolder.getId(),
                        normalizedTitle
                );
        if (alreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Le dossier existe déjà"
            );
        }

        DbFolder entity = createFolderInputMapper.toEntity(input, user, parentFolder);
        DbFolder saved = folderRepository.save(entity);
        return createFolderOutputMapper.toCreateOutput(saved);
    }

}
