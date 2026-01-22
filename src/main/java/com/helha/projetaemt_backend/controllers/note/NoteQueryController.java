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
            summary = "Get a note by ID",
            description = "Returns the requested note. 404 if the note does not exist."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    public ResponseEntity<GetByIdNoteOutput> findById(@PathVariable int idNote){
        return ResponseEntity.ok(noteQueryProcessor.getByIdNoteHandler.handle(idNote));
    }

    @Operation(
            summary = "Get notes by folder",
            description = """
        Returns the list of notes in a folder.
        404 if the folder does not exist.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Notes list retrieved",
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
            summary = "Export a note to PDF",
            description = """
            Generates a PDF file containing the note and its metadata.
            The file is returned for download.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "PDF generated successfully",
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
                    description = "Error generating PDF",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}/export-pdf")
    public ResponseEntity<byte[]> exportToPdf(@PathVariable int id) throws Exception{
        String appBaseUrl = "https://monapp";

        byte[] pdfBytes = noteQueryProcessor.pdfExportHandler.handle(id, appBaseUrl);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=note_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
