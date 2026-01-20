package com.helha.projetaemt_backend.infrastructure.note;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface INoteRepository extends CrudRepository<DbNote, Integer> {
    @EntityGraph(attributePaths = {"user", "folder"})
    List<DbNote> findByFolderId(int id);
    List<DbNote> findAllByUser_Id(int userId);
}
