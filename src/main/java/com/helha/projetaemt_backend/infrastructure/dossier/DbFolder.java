package com.helha.projetaemt_backend.infrastructure.dossier;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "folder")
public class DbFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long id;
    @Column(name = "id_user", nullable = false)
    public long userId;
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
