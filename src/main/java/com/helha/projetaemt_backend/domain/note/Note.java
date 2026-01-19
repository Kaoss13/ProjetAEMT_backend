package com.helha.projetaemt_backend.domain.note;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class Note {

    private int idUser;
    private int idFolder;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int syzeBytes;
    private int lineCount;
    private int wordCount;
    private int charCount;

}
