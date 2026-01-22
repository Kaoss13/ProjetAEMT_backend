package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.query.FolderQueryProcessor;
import com.helha.projetaemt_backend.application.folder.query.getAll.GetAllFoldersWithNotesOutput;
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
@RequestMapping("/folders")
public class FolderQueryController {
    private final FolderQueryProcessor folderQueryProcessor;

    public FolderQueryController(FolderQueryProcessor folderQueryProcessor) {
        this.folderQueryProcessor = folderQueryProcessor;
    }

    @Operation(
            summary = "Récupérer tous les dossiers et notes d’un utilisateur",
            description = """
                Retourne deux listes plates :
                - folders
                - notes

                Le frontend reconstruit l’arbre via buildFolderTree(folders, notes).
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Folders et notes récupérés",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GetAllFoldersWithNotesOutput.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/all/{userId}")
    public ResponseEntity<GetAllFoldersWithNotesOutput> getAllFoldersWithNotes(@PathVariable int userId) {
        GetAllFoldersWithNotesOutput output = folderQueryProcessor
                .getAllFoldersWithNotesHandler
                .handle(userId);

        return ResponseEntity.ok(output);
    }

    @Operation(
            summary = "Exporter un dossier en ZIP",
            description = """
            Génère une archive ZIP contenant le dossier,
            ses sous-dossiers et les notes associées.
            Le fichier est retourné en téléchargement.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Archive ZIP générée avec succès",
                    content = @Content(
                            mediaType = "application/octet-stream"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erreur lors de la génération du ZIP",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @GetMapping("/{id}/export-zip")
    public ResponseEntity<byte[]> exportFolderToZip(@PathVariable int id) throws Exception {

        byte[] zipBytes = folderQueryProcessor.zipExportHandler.handle(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=folder_" + id + ".zip")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(zipBytes);
    }
}
