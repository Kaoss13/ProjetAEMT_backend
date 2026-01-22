package com.helha.projetaemt_backend.controllers.note;

import com.helha.projetaemt_backend.application.note.query.NoteQueryProcessor;
import com.helha.projetaemt_backend.application.note.query.getbyid.GetByIdNoteOutput;
import com.helha.projetaemt_backend.application.note.query.getbyidfolder.GetByIdFolderNoteOutput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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

    @Operation(
            summary = "Récupérer une note par ID",
            description = "Retourne la note demandée. 404 si la note n'existe pas."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note trouvée"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("{idNote}")
    public ResponseEntity<GetByIdNoteOutput> findById(@PathVariable int idNote){
        return ResponseEntity.ok(noteQueryProcessor.getByIdNoteHandler.handle(idNote));
    }

    @Operation(
            summary = "Récupérer les notes d'un dossier",
            description = """
        Retourne la liste des notes d'un dossier.
        404 si le dossier n'existe pas.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Liste des notes récupérée",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetByIdFolderNoteOutput.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/folders/{idFolder}")
    public ResponseEntity<GetByIdFolderNoteOutput> findByIdFolder(@PathVariable int idFolder){
        return ResponseEntity.ok(noteQueryProcessor.getByIdFolderNoteHandler.handle(idFolder));
    }

    @Operation(
            summary = "Exporter une note en PDF",
            description = """
            Génère un fichier PDF contenant la note et ses métadonnées.
            Le fichier est retourné en téléchargement.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF généré avec succès",
                    content = @Content(
                            mediaType = "application/pdf"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la génération du PDF",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable int id) throws Exception{
        byte[] pdfBytes = noteQueryProcessor.pdfExportHandler.handle(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=note_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
