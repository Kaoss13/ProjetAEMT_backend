package com.helha.projetaemt_backend.infrastructure.dossier;

import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "folder")
public class DbFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_user", nullable = false)
    public DbUser user;

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
