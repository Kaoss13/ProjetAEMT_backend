package com.helha.projetaemt_backend.infrastructure.note;

import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class DbNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    public DbUser user;

    @ManyToOne
    @JoinColumn(name = "id_folder")
    public DbFolder folder;

    public String title;
    public String content;
    @Column(name = "created_at")
    public LocalDateTime createdAt;
    @Column(name = "updated_at")
    public LocalDateTime updatedAt;
}
