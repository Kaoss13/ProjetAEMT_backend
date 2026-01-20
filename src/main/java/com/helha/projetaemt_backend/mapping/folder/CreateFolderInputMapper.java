package com.helha.projetaemt_backend.mapping.folder;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderInput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
/*Input -> Entity*/
public class CreateFolderInputMapper {

    public DbFolder toEntity(CreateFolderInput input, DbUser user, DbFolder parentFolder){
        DbFolder entity = new DbFolder();

        entity.setTitle(input.title.trim());
        entity.setUser(user);
        entity.setParentFolder(parentFolder);

        return entity;
    }
}
