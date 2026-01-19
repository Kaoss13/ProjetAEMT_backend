package com.helha.projetaemt_backend.infrastructure.dossier;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IFolderRepository extends CrudRepository<DbFolder, Long> {
    //Test
}
