package com.helha.projetaemt_backend;

import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderInput;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderOutput;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import jakarta.persistence.EntityManager;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper(EntityManager em) {
        ModelMapper modelMapper = new ModelMapper();

        // Autoriser lecture/écriture sur CHAMPS PUBLICS + ignorer les nulls
        modelMapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setSkipNullEnabled(true);

        TypeMap<DbNote, GetByIdNoteOutput> typeMap =
                modelMapper.createTypeMap(DbNote.class, GetByIdNoteOutput.class);

        typeMap.setPostConverter(ctx -> {
            DbNote s = ctx.getSource();
            GetByIdNoteOutput d = ctx.getDestination();
            if (s == null || d == null) return d;

            d.idUser   = (s.user   != null ? s.user.id   : 0);
            d.idFolder = Math.toIntExact((s.folder != null ? s.folder.id : 0));
            return d;
        });
        // -------- FOLDER : converters
        Converter<Integer, DbUser> userIdToUserRef = ctx -> {
            Integer id = ctx.getSource();
            if (id == null) return null;
            return em.getReference(DbUser.class, id);
        };

        Converter<Integer, DbFolder> folderIdToFolderRef = ctx -> {
            Integer id = ctx.getSource();
            if (id == null) return null;
            return em.getReference(DbFolder.class, id);
        };

        // -------- FOLDER : CreateFolderInput -> DbFolder
        TypeMap<CreateFolderInput, DbFolder> inToEntity =
                modelMapper.createTypeMap(CreateFolderInput.class, DbFolder.class);

        inToEntity.addMappings(mapper -> {
            mapper.using(userIdToUserRef).map(src -> src.userId, DbFolder::setUser);
            mapper.using(folderIdToFolderRef)
                    .map(src -> src.parentFolderId, (DbFolder dest, DbFolder v) -> dest.parentFolder = v);
            mapper.map(src -> src.title, (dest, v) -> dest.title = (String) v);
        });

        // -------- FOLDER : DbFolder -> CreateFolderOutput
        TypeMap<DbFolder, CreateFolderOutput> entityToOut =
                modelMapper.createTypeMap(DbFolder.class, CreateFolderOutput.class);

        entityToOut.setPostConverter(ctx -> {
            DbFolder s = ctx.getSource();
            CreateFolderOutput d = ctx.getDestination();
            if (s == null || d == null) return d;

            d.userId = (s.getUser() != null ? s.getUser().id : 0);
            d.parentFolderId = (s.parentFolder != null ? s.parentFolder.id : null);
            return d;
        });
        return modelMapper;
    }

}



