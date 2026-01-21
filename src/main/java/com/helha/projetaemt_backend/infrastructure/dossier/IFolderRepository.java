package com.helha.projetaemt_backend.infrastructure.dossier;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IFolderRepository extends CrudRepository<DbFolder, Integer> {
    //Si même dossier racine ==> Interdit les doublons entre dossiers racine
    boolean existsByUser_IdAndParentFolderIsNullAndTitleIgnoreCase(int userId, String title);
    //Si même sous dossier dans le même dossier racine ==> Interdit les doublons entre enfants du même parent
    boolean existsByUser_IdAndParentFolder_IdAndTitleIgnoreCase(int userId, int parentFolderId, String title);
    boolean existsByIdAndUser_Id(int folderId, int userId);
    List<DbFolder> findAllByUser_Id(int userId);
    List<DbFolder> findByParentFolder_Id(int id);
    Optional<DbFolder> findByUser_IdAndParentFolderIsNull(int idUser);

    //Optional<DbFolder> findByUser_IdAndParentFolderEqualsIsNullAndIdEquals0(int idUser);
}
