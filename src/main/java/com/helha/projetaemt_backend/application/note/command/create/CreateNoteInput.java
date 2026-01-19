package com.helha.projetaemt_backend.application.note.command.create;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public class CreateNoteInput {

    public int id;
    public int idUser;
    public int idFolder;
    public String title;
    public String content;

}
