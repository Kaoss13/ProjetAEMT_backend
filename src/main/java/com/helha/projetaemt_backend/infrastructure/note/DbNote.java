package com.helha.projetaemt_backend.infrastructure.note;

import com.helha.projetaemt_backend.domain.folder.Folder;
import com.helha.projetaemt_backend.domain.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class DbNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    public User user;

    @ManyToOne
    @JoinColumn(name = "id_folder")
    public Folder folder;

    public String title;
    public String content;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;
    public int syzeBytes;
    public int lineCount;
    public int wordCount;
    public int charCount;
}
