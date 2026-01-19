package com.helha.projetaemt_backend.infrastructure.user;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
public class DbUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @Column(name = "user_name", nullable = false, unique = true)
    public String userName;

    @Column(name = "hash_password", nullable = false)
    public String hashPassword;


}
