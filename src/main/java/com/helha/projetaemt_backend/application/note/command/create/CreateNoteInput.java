package com.helha.projetaemt_backend.application.note.command.create;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CreateNoteInput {

    public int idUser;
    public int idFolder;
    public String title;
    public String content;

}
