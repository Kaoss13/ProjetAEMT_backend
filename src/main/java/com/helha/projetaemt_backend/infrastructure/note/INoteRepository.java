package com.helha.projetaemt_backend.infrastructure.note;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface INoteRepository extends CrudRepository<DbNote, Integer> {
}
