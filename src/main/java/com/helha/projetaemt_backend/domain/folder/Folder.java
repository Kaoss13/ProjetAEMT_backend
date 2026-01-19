package com.helha.projetaemt_backend.domain.folder;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Folder {
    private int id;
    private int userId;
    private int parentFolderId;
    private String title;
    private LocalDateTime createdAt;
}
