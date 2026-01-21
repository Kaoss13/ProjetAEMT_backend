package com.helha.projetaemt_backend.application.folder.query.getAll;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GetAllFoldersWithNotesHandler {
    private final IFolderRepository folderRepository;
    private final INoteRepository noteRepository;
    private final IUserRepository userRepository;

    public GetAllFoldersWithNotesHandler(IFolderRepository folderRepository,
                                         INoteRepository noteRepository,
                                         IUserRepository userRepository) {
        this.folderRepository = folderRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public GetAllFoldersWithNotesOutput handle(int userId) {

        // Vérifier que le user existe
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("The user with this ID does not exist");
        }

        List<DbFolder> folders = folderRepository.findAllByUser_Id(userId);
        List<DbNote> notes = noteRepository.findAllByUser_Id(userId);

        GetAllFoldersWithNotesOutput output = new GetAllFoldersWithNotesOutput();

        // Mapper les dossiers
        for (DbFolder f : folders) {
            GetAllFoldersWithNotesOutput.FolderDto dto = new GetAllFoldersWithNotesOutput.FolderDto();
            dto.id = f.id;
            dto.userId = f.user.id;
            dto.id_parent_folder = (f.parentFolder == null) ? null : f.parentFolder.id;
            dto.title = f.title;
            output.folders.add(dto);
        }

        // Mapper les notes avec métadonnées calculées
        for (DbNote n : notes) {
            if (n.folder == null) continue;

            GetAllFoldersWithNotesOutput.NoteDto dto = new GetAllFoldersWithNotesOutput.NoteDto();
            dto.id = n.id;
            dto.id_user = n.user.id;
            dto.id_folder = n.folder.id;
            dto.title = n.title;
            dto.content = n.content;
            dto.createdAt = n.createdAt;
            dto.updatedAt = n.updatedAt;

            // Calcul des métadonnées via le domaine
            String content = n.content != null ? n.content : "";
            Note noteDomain = new Note();
            noteDomain.setContent(content);

            dto.sizeBytes = noteDomain.getSizeBytes();
            dto.lineCount = noteDomain.getLineCount();
            dto.wordCount = noteDomain.getWordCount();
            dto.charCount = noteDomain.getCharCount();

            output.notes.add(dto);
        }

        return output;
    }
}
