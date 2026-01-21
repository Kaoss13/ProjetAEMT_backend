package com.helha.projetaemt_backend.application.note.command.create;

import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class CreateNoteHandler {

    private final INoteRepository noteRepository;
    private final IUserRepository userRepository;
    private final IFolderRepository folderRepository;
    private final NoteMapper noteMapper;

    public CreateNoteHandler(INoteRepository noteRepository, IUserRepository userRepository,
                             IFolderRepository folderRepository, NoteMapper noteMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.noteMapper = noteMapper;
    }

    public CreateNoteOutput handle(CreateNoteInput input) {

        DbUser user = userRepository.findById(input.idUser)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DbFolder folder = folderRepository.findById(input.idFolder)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        Note noteDomain = Note.builder()
                .idUser(input.idUser)
                .idFolder(input.idFolder)
                .title(input.title)
                .content(input.content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        DbNote saved = noteRepository.save(noteMapper.toEntity(noteDomain, user, folder));

        return noteMapper.map(saved, CreateNoteOutput.class);
    }
}


