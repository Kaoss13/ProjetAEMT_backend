package com.helha.projetaemt_backend.application.note.command.create;

import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Utilisateur introuvable"
                ));

        DbFolder folder;
        if (input.idFolder != 0) {
            folder = folderRepository.findById(input.idFolder)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Dossier introuvable"
                    ));
        } else {
            folder = folderRepository.findByUser_IdAndParentFolderIsNull(input.idUser)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Dossier racine introuvable"
                    ));
        }


        Note noteDomain = Note.builder()
                .idUser(input.idUser)
                .idFolder(input.idFolder == 0 ? folder.id : input.idFolder)
                .title(input.title)
                .content(input.content)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        DbNote saved = noteRepository.save(noteMapper.toEntity(noteDomain, user, folder));

        return noteMapper.map(saved, CreateNoteOutput.class);
    }
}


