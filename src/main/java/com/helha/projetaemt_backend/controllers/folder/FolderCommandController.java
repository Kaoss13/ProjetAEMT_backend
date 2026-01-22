package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.command.FolderCommandProcessor;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderInput;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderOutput;
import com.helha.projetaemt_backend.application.folder.command.update.UpdateFolderInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/folders")
public class FolderCommandController {
    private final FolderCommandProcessor folderCommandProcessor;

    public FolderCommandController(FolderCommandProcessor folderCommandProcessor){
        this.folderCommandProcessor = folderCommandProcessor;
    }

    @Operation(
            summary = "Créer un dossier",
            description = """
                Crée un nouveau dossier pour un utilisateur donné.
                - Le titre est obligatoire et ne peut pas dépasser 255 caractères.
                - parentFolderId = null => dossier racine
                - parentFolderId != null => sous-dossier
                - Un sous-dossier ne peut être créé que dans un dossier appartenant au même utilisateur.
                Retourne 201 avec l'en-tête Location pointant vers la ressource créée.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Folder successfully created",
                    headers = @Header(
                            name = "Location",
                            description = "Location of the created folder resource"
                    ),
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateFolderOutput.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: validation error (title blank/too long) or business rule violation (parent folder does not belong to user)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found: user not found, root folder not found for this user, or parent folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: folder title already exists in the same parent folder for this user",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping()
    public ResponseEntity<CreateFolderOutput> create(@Valid @RequestBody CreateFolderInput input){
        CreateFolderOutput output = folderCommandProcessor
                .createFolderHandler
                .handle(input);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(output.id)
                .toUri();
        return ResponseEntity
                .created(location)
                .body(output);
    }

    @Operation(
            summary = "Renommer un dossier",
            description = """
            Renomme un dossier existant.
    
            Règles :
            - Le titre est obligatoire et ne peut pas dépasser 255 caractères.
            - Le dossier doit exister.
            - Le dossier doit appartenir à l'utilisateur fourni (userId).
            - Le nouveau titre doit être unique au même niveau (même parentFolder) pour un même utilisateur.
    
            Remarque :
            - Cette opération ne déplace pas le dossier (pas de modification de parentFolder).
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Folder successfully renamed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request: validation error (title blank/too long) or business rule violation (folder does not belong to user)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found: folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: a folder with the same title already exists at the same level for this user",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PutMapping()
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateFolderInput input) {
        folderCommandProcessor.updateFolderHandler.handle(input);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Supprimer un dossier",
            description = """
            Supprime un dossier par son identifiant.
            - La suppression est récursive : les sous-dossiers et les notes associées sont supprimés via ON DELETE CASCADE.
            Retourne 204 si la suppression a réussi.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Folder successfully deleted"
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
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable int folderId){
        folderCommandProcessor.deleteFolderHandler.handle(folderId);
        return ResponseEntity.noContent().build();
    }
}