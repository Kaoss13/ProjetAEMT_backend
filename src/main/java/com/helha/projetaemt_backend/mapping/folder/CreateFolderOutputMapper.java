package com.helha.projetaemt_backend.mapping.folder;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderOutput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
/*Entity -> Output*/
public class CreateFolderOutputMapper {

    private final ModelMapper modelMapper;

    public CreateFolderOutputMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CreateFolderOutput toCreateOutput(DbFolder entity) {
        if (entity == null) return null;

        CreateFolderOutput dto =
                modelMapper.map(entity, CreateFolderOutput.class);

        dto.userId = entity.getUser() != null ? entity.getUser().id : 0;
        dto.parentFolderId =
                entity.getParentFolder() != null ? entity.getParentFolder().id : null;

        return dto;
    }
}

