package com.helha.projetaemt_backend;

import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
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

        return modelMapper;
    }

}



