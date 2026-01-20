package com.helha.projetaemt_backend.application.note.query;

import com.helha.projetaemt_backend.application.exportation.PdfExportHandler;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteHandler;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteOutput;
import org.springframework.stereotype.Service;

@Service
public class NoteQueryProcessor {
    public final GetByIdNoteHandler getByIdNoteHandler;
    public final GetByIdFolderNoteHandler getByIdFolderNoteHandler;
    public final PdfExportHandler pdfExportHandler;

    public NoteQueryProcessor(GetByIdNoteHandler getByIdNoteHandler, GetByIdFolderNoteHandler getByIdFolderNoteHandler, PdfExportHandler pdfExportHandler) {
        this.getByIdNoteHandler = getByIdNoteHandler;
        this.getByIdFolderNoteHandler = getByIdFolderNoteHandler;
        this.pdfExportHandler = pdfExportHandler;
    }
}
