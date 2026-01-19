package com.helha.projetaemt_backend.domain.user;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int id;

    @Column(name = "user_name")
    public String userName;

    @Column(name = "hash_password")
    public String hashPassword;
}
