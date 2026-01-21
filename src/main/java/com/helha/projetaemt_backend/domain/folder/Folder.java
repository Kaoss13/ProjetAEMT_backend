package com.helha.projetaemt_backend.domain.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Folder {
    private int id;
    private int userId;
    private int parentFolderId;
    private String title;
    private LocalDateTime createdAt;

    public Folder(int id, int userId, int parentFolderId, String title){
        this.id = id;
        this.userId = userId;
        this.parentFolderId = parentFolderId;
        this.title = title;
    }
}