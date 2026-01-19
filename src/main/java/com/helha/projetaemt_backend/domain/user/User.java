package com.helha.projetaemt_backend.domain.user;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class User {

    public int id;
    public String userName;

    public String hashPassword;
}
