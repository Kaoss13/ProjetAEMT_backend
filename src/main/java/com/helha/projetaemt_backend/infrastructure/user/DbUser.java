package com.helha.projetaemt_backend.infrastructure.user;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DbUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "user_name", nullable = false, unique = true)
    public String userName;

    @Column(name = "hash_password", nullable = false)
    public String hashPassword;


}
