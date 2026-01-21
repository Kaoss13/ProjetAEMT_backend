package com.helha.projetaemt_backend.domain.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    private int id;
    private int idUser;
    private int idFolder;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



    public Note (int id, int idUser, int idFolder, String title, String content){
        this.id = id;
        this.idUser = idUser;
        this.idFolder = idFolder;
        this.title = title;
        this.content = content;
    }

    public int computeSizeBytes(){
        return this.content.getBytes(StandardCharsets.UTF_8).length;
    }
    public int computeLineCount(){
        return this.content.split("\r\n|\r|\n").length;
    }
    public int computeWordCount(){
        return this.content.trim().isEmpty()
                ? 0
                : this.content.trim().split("\\s+").length;
    }
    public int computeCharCount(){
        return this.content.length();
    }
}
