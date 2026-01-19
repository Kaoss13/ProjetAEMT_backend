package com.helha.projetaemt_backend.application.note.command.create;

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
    private final ModelMapper modelMapper;


    public CreateNoteHandler(INoteRepository noteRepository, IUserRepository userRepository, IFolderRepository folderRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.modelMapper = modelMapper;
    }


    public CreateNoteOutput handle(CreateNoteInput input) {

        DbUser user = userRepository.findById(input.idUser)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DbFolder folder = folderRepository.findById((long) input.idFolder)
                .orElseThrow(() -> new RuntimeException("Folder not found"));


        DbNote note = new DbNote();
        note.user = user;
        note.folder = folder;

        note.title = input.title;
        note.content = input.content;

        // === CALCUL AUTOMATIQUE ===
        note.createdAt = LocalDateTime.now();
        note.updatedAt = LocalDateTime.now();

        note.sizeBytes = input.content.getBytes(StandardCharsets.UTF_8).length;
        note.lineCount = input.content.split("\r\n|\r|\n").length;
        note.wordCount = input.content.trim().isEmpty()
                ? 0
                : input.content.trim().split("\\s+").length;
        note.charCount = input.content.length();

        // Persist
        DbNote saved = noteRepository.save(note);

        // === MAPPING VERS OUTPUT ===
        CreateNoteOutput output = modelMapper.map(saved, CreateNoteOutput.class);
        output.idUser = saved.user.id;
        output.idFolder = Math.toIntExact(saved.folder.id);

        return output;
    }

    }
