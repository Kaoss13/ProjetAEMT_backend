package com.helha.projetaemt_backend.application.folder.query.getAll;

import com.helha.projetaemt_backend.domain.note.Note;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.note.DbNote;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
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
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Utilisateur introuvable"
            );
        }

        List<DbFolder> folders = folderRepository.findAllByUser_Id(userId);
        List<DbNote> notes = noteRepository.findAllByUser_Id(userId);

        //Tri alphabétique
        folders.sort(Comparator.comparing(
                f -> f.title == null ? "" : f.title,
                String.CASE_INSENSITIVE_ORDER
        ));

        notes.sort(Comparator.comparing(
                n -> n.title == null ? "" : n.title,
                String.CASE_INSENSITIVE_ORDER
        ));

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

            dto.sizeBytes = noteDomain.computeSizeBytes();
            dto.lineCount = noteDomain.computeLineCount();
            dto.wordCount = noteDomain.computeWordCount();
            dto.charCount = noteDomain.computeCharCount();

            output.notes.add(dto);
        }

        return output;
    }
}
