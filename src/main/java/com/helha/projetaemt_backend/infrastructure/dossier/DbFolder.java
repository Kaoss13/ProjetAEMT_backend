package com.helha.projetaemt_backend.infrastructure.dossier;

import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "folder")
public class DbFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    private DbUser user;

    // Relation récursive : dossier parent
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_parent_folder")
    public DbFolder parentFolder;

    @Column(nullable = false, length = 255)
    public String title;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public LocalDateTime createdAt;
}
