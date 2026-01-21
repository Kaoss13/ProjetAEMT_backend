
package com.helha.projetaemt_backend;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

@Configuration
public class NoteMapper {
    private final DbNote entity = new DbNote();

    private final ModelMapper modelMapper;

    public NoteMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <T> T map(DbNote entity, Class<T> outputClass) {
        if (entity == null) return null;

        // 1) Mapping standard
        T dto = modelMapper.map(entity, outputClass);

        // 2) idUser (association -> fallback champs primitifs)
        Integer idUser = null;
        try {
            if (entity.user != null) {
                idUser = entity.user.id;
            }
        } catch (Exception ignored) {}
        if (idUser == null || idUser == 0) {
            Integer fromField = readIntFieldIfExists(entity, "idUser");
            if (fromField == null) fromField = readIntFieldIfExists(entity, "userId");
            if (fromField != null && fromField != 0) {
                idUser = fromField;
            }
        }
        if (idUser != null) {
            setIfExists(dto, "idUser", idUser);
        }

        // 3) idFolder (association -> fallback champs primitifs)
        Integer idFolder = null;
        try {
            if (entity.folder != null) {
                idFolder = entity.folder.id;
            }
        } catch (Exception ignored) {}
        if (idFolder == null || idFolder == 0) {
            Integer fromField = readIntFieldIfExists(entity, "idFolder");
            if (fromField == null) fromField = readIntFieldIfExists(entity, "folderId");
            if (fromField != null && fromField != 0) {
                idFolder = fromField;
            }
        }
        if (idFolder != null) {
            setIfExists(dto, "idFolder", idFolder);
        }

        // 4) Calcul des métadonnées à la volée via le domaine
        try {
            String content = entity.content != null ? entity.content : "";
            Note noteDomain = new Note();
            noteDomain.setContent(content);

            setIfExists(dto, "sizeBytes", noteDomain.getSizeBytes());
            setIfExists(dto, "lineCount", noteDomain.getLineCount());
            setIfExists(dto, "wordCount", noteDomain.getWordCount());
            setIfExists(dto, "charCount", noteDomain.getCharCount());
        } catch (Exception ignored) {
            // Si le DTO n'a pas ces champs, on ignore
        }

        return dto;
    }

    private <T> void setIfExists(T dto, String fieldName, Object value) {
        try {
            Field field = dto.getClass().getField(fieldName); // champ public
            field.set(dto, value);
        } catch (NoSuchFieldException ignored) {
            // DTO sans ce champ -> ignorer
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer readIntFieldIfExists(Object source, String fieldName) {
        try {
            Field f = source.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            Object v = f.get(source);
            if (v instanceof Number) {
                return ((Number) v).intValue();
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {}
        return null;
    }


    public DbNote toEntity(Note noteDomain, DbUser user, DbFolder folder) {
        entity.user = user;
        entity.folder = folder;
        entity.title = noteDomain.getTitle();
        entity.content = noteDomain.getContent();
        entity.createdAt = noteDomain.getCreatedAt();
        entity.updatedAt = noteDomain.getUpdatedAt();
        return entity;
    }

}

