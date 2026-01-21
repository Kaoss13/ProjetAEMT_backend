package com.helha.projetaemt_backend.application.folder.command.create;

import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import com.helha.projetaemt_backend.mapping.folder.CreateFolderInputMapper;
import com.helha.projetaemt_backend.mapping.folder.CreateFolderOutputMapper;
import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import org.springframework.stereotype.Service;

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

        if (input.title == null || input.title.trim().isEmpty()) {
            throw new IllegalArgumentException("The folder title is mandatory.");
        }

        DbUser user = userRepository.findById(input.userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        DbFolder parentFolder;

        if (input.parentFolderId == null || input.parentFolderId == 0) {
            parentFolder = folderRepository.findByUser_IdAndParentFolderIsNull(input.userId)
                    .orElseThrow(() -> new IllegalArgumentException("Root folder not found for this user."));
        }
        else {
            parentFolder = folderRepository.findById(input.parentFolderId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent folder not found"));

            if (parentFolder.user.id != input.userId) {
                throw new IllegalArgumentException("Parent folder does not belong to this user.");
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
            throw new IllegalArgumentException("Folder already exists");
        }

        DbFolder entity = createFolderInputMapper.toEntity(input, user, parentFolder);
        DbFolder saved = folderRepository.save(entity);

        return createFolderOutputMapper.toCreateOutput(saved);
    }

}
