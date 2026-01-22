package com.helha.projetaemt_backend.domain.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.Jsoup;

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


    private String getPlainText() {
        return content != null ? Jsoup.parse(content).text() : "";
    }

    public int computeSizeBytes(){
        return getPlainText().getBytes(StandardCharsets.UTF_8).length;
    }
    public int computeLineCount(){
        return getPlainText().isEmpty() ? 0 : getPlainText().split("\r\n|\r|\n").length;
    }
    public int computeWordCount(){
        String plainText = getPlainText().trim();
        return plainText.isEmpty() ? 0 : plainText.split("\\s+").length;

    }
    public int computeCharCount(){
        return getPlainText().length();
    }
}
