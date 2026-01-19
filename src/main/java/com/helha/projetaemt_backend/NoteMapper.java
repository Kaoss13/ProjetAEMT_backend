package com.helha.projetaemt_backend;

import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;

@Configuration
public class NoteMapper {

    private final ModelMapper modelMapper;

    public NoteMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <T> T map(DbNote entity, Class<T> outputClass) {
        if (entity == null) return null;

        // Mapping standard (ModelMapper)
        T dto = modelMapper.map(entity, outputClass);

        // Ajout automatique du idUser si présent
        setIfExists(dto, "idUser",
                entity.user != null ? entity.user.id : 0);

        // Ajout automatique du idFolder si présent
        setIfExists(dto, "idFolder",
                entity.folder != null ? entity.folder.id : 0);

        return dto;
    }

    private <T> void setIfExists(T dto, String fieldName, Object value) {
        try {
            Field field = dto.getClass().getField(fieldName); // cherche un champ public
            field.set(dto, value);
        } catch (NoSuchFieldException ignored) {
            // le champ n'existe pas dans ce DTO → on ignore
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
