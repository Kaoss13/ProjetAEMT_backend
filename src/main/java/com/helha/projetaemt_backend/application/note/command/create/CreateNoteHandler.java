package com.helha.projetaemt_backend.application.note.command.create;

import com.helha.projetaemt_backend.domain.note.Note;
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

    public CreateNoteHandler(INoteRepository noteRepository, IUserRepository userRepository,
                             IFolderRepository folderRepository, ModelMapper modelMapper) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.folderRepository = folderRepository;
        this.modelMapper = modelMapper;
    }

    public CreateNoteOutput handle(CreateNoteInput input) {

        DbUser user = userRepository.findById(input.idUser)
                .orElseThrow(() -> new RuntimeException("User not found"));

        DbFolder folder = folderRepository.findById(input.idFolder)
                .orElseThrow(() -> new RuntimeException("Folder not found"));

        Note noteDomain = new Note();
        noteDomain.setIdUser(input.idUser);
        noteDomain.setIdFolder(input.idFolder);
        noteDomain.setTitle(input.title);
        noteDomain.setContent(input.content);
        noteDomain.setCreatedAt(LocalDateTime.now());
        noteDomain.setUpdatedAt(LocalDateTime.now());

        DbNote dbNote = new DbNote();
        dbNote.user = user;
        dbNote.folder = folder;
        dbNote.title = noteDomain.getTitle();
        dbNote.content = noteDomain.getContent();
        dbNote.createdAt = noteDomain.getCreatedAt();
        dbNote.updatedAt = noteDomain.getUpdatedAt();

        DbNote saved = noteRepository.save(dbNote);

        CreateNoteOutput output = modelMapper.map(saved, CreateNoteOutput.class);
        output.idUser = saved.user.id;
        output.idFolder = Math.toIntExact(saved.folder.id);

        output.sizeBytes = noteDomain.getSizeBytes();
        output.lineCount = noteDomain.getLineCount();
        output.wordCount = noteDomain.getWordCount();
        output.charCount = noteDomain.getCharCount();
        return output;
    }
}

