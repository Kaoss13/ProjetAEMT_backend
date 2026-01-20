package com.helha.projetaemt_backend.controllers.note;

import com.helha.projetaemt_backend.application.note.exceptions.NoteNotFoundException;
import com.helha.projetaemt_backend.application.note.query.NoteQueryProcessor;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteOutput;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notes")
public class NoteQueryController {
    private final NoteQueryProcessor noteQueryProcessor;

    public NoteQueryController(NoteQueryProcessor noteQueryProcessor) {
        this.noteQueryProcessor = noteQueryProcessor;
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a note is not found.",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("{idNote}")
    public ResponseEntity<GetByIdNoteOutput> findById(@PathVariable int idNote){
        try {
            return ResponseEntity.ok(noteQueryProcessor.getByIdNoteHandler.handle(idNote));
        }catch (IllegalArgumentException e){
            throw new NoteNotFoundException(idNote);
        }

    }

    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "404",
                    description = "When a note is not found.",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @GetMapping("/folders/{idFolder}")
    public ResponseEntity<GetByIdFolderNoteOutput> findByIdFolder(@PathVariable int idFolder){
        return ResponseEntity.ok(noteQueryProcessor.getByIdFolderNoteHandler.handle(idFolder));
    }

    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable int id) throws Exception{
        byte[] pdfBytes = noteQueryProcessor.pdfExportHandler.handle(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=note_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
