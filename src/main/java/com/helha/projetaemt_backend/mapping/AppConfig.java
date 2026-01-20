package com.helha.projetaemt_backend.mapping;

import jakarta.persistence.EntityManager;
import org.modelmapper.ModelMapper;
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

        return modelMapper;
    }
}



