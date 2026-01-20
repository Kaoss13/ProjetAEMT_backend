package com.helha.projetaemt_backend.infrastructure.user;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends CrudRepository<DbUser, Integer> {

    Optional<DbUser> findByUserName(String userName);

    DbUser searchByUserName(String userName);

    boolean existsByUserName(String userName);

    Optional<DbUser> findById(Integer id);

}
