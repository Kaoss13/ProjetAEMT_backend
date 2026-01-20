package com.helha.projetaemt_backend.application.folder.query;

import com.helha.projetaemt_backend.application.folder.query.getAll.GetAllFoldersWithNotesHandler;
import org.springframework.stereotype.Service;

@Service
public class FolderQueryProcessor {
    public final GetAllFoldersWithNotesHandler getAllFoldersWithNotesHandler;

    public FolderQueryProcessor(GetAllFoldersWithNotesHandler getAllFoldersWithNotesHandler){
        this.getAllFoldersWithNotesHandler = getAllFoldersWithNotesHandler;
    }
}
