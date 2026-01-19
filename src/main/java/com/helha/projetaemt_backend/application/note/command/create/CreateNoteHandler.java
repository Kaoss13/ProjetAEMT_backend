package com.helha.projetaemt_backend.application.note.command.create;

import com.helha.projetaemt_backend.NoteInputMapper;
import com.helha.projetaemt_backend.NoteMapper;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CreateNoteHandler {

    private final INoteRepository noteRepository;
    private final IUserRepository userRepository;
    private final IFolderRepository folderRepository;
    private final NoteMapper noteMapper;
    private final ModelMapper modelMapper;
    private final NoteInputMapper noteInputMapper;


    public CreateNoteHandler(INoteRepository noteRepository, IUserRepository userRepository, IFolderRepository folderRepository, NoteMapper noteMapper, ModelMapper modelMapper, NoteInputMapper noteInputMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.noteMapper = noteMapper;
        this.modelMapper = modelMapper;
        this.noteInputMapper = noteInputMapper;
    }


    public CreateNoteOutput handle(CreateNoteInput input) {

        DbUser user = userRepository.findById(input.idUser)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DbFolder folder = folderRepository.findById((long) input.idFolder)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        // Toute la construction est maintenant déléguée au mapper
        DbNote entity = noteInputMapper.toEntity(input, user, folder);

        DbNote savedEntity = noteRepository.save(entity);

        return noteMapper.map(savedEntity, CreateNoteOutput.class);

    }

}
