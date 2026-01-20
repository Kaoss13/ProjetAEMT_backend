package com.helha.projetaemt_backend.application.note.command.update;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateNoteInput {
    public int id;
    public String title;
    public String content;

}
