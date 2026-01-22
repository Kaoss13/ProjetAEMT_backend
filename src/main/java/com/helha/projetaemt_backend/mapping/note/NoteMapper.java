package com.helha.projetaemt_backend.mapping.note;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

/**
 * Mapper responsible for converting between DbNote entities and DTOs or domain objects.
 * It uses ModelMapper for standard mapping and adds custom logic for specific fields.
 */
@Configuration
public class NoteMapper {

    private final ModelMapper modelMapper;

    /**
     * Constructor injecting ModelMapper.
     *
     * @param modelMapper The ModelMapper instance used for object mapping.
     */
    public NoteMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * Maps a DbNote entity to a DTO of the specified type.
     * Includes custom logic for user ID, folder ID, and computed metadata.
     *
     * @param entity       The DbNote entity to map.
     * @param outputClass  The target DTO class.
     * @param <T>          Generic type of the output class.
     * @return Mapped DTO instance or null if entity is null.
     */
    public <T> T map(DbNote entity, Class<T> outputClass) {
        if (entity == null) return null;

        // 1) Standard mapping using ModelMapper
        T dto = modelMapper.map(entity, outputClass);

        // 2) Handle idUser (association -> fallback to primitive fields)
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

        // 3) Handle idFolder (association -> fallback to primitive fields)
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

        // 4) Compute metadata dynamically using the domain object
        try {
            String content = entity.content != null ? entity.content : "";
            Note noteDomain = new Note();
            noteDomain.setContent(content);

            setIfExists(dto, "sizeBytes", noteDomain.computeSizeBytes());
            setIfExists(dto, "lineCount", noteDomain.computeLineCount());
            setIfExists(dto, "wordCount", noteDomain.computeWordCount());
            setIfExists(dto, "charCount", noteDomain.computeCharCount());
        } catch (Exception ignored) {
            // If the DTO does not have these fields, ignore
        }

        return dto;
    }

    /**
     * Sets a field value on the DTO if the field exists and is accessible.
     *
     * @param dto       The target DTO object.
     * @param fieldName The name of the field to set.
     * @param value     The value to assign.
     */
    private <T> void setIfExists(T dto, String fieldName, Object value) {
        try {
            Field field = dto.getClass().getField(fieldName); // public field
            field.set(dto, value);
        } catch (NoSuchFieldException ignored) {
            // DTO does not have this field -> ignore
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads an integer field from the source object if it exists.
     *
     * @param source    The source object.
     * @param fieldName The name of the field to read.
     * @return Integer value or null if not found.
     */
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

    /**
     * Converts a domain Note object to a DbNote entity.
     *
     * @param noteDomain The domain Note object.
     * @param user       Associated user entity.
     * @param folder     Associated folder entity.
     * @return DbNote entity populated with data from the domain object.
     */
    public DbNote toEntity(Note noteDomain, DbUser user, DbFolder folder) {
        DbNote entity = new DbNote();
        entity.user = user;
        entity.folder = folder;
        entity.title = noteDomain.getTitle();
        entity.content = noteDomain.getContent();
        entity.createdAt = noteDomain.getCreatedAt();
        entity.updatedAt = noteDomain.getUpdatedAt();
        return entity;
    }
}
