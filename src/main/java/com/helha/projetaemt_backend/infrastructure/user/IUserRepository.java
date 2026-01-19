package com.helha.projetaemt_backend.infrastructure.user;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface IUserRepository extends CrudRepository<DbUser, Long> {

    Optional<DbUser> findByUserName(String userName);

    DbUser searchByUserName(String userName);

    boolean existsByUserName(String userName);

    Optional<DbUser> findById(Integer id);

}
