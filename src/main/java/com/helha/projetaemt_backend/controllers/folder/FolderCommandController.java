package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.command.FolderCommandProcessor;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderInput;
import com.helha.projetaemt_backend.application.folder.command.create.CreateFolderOutput;
import com.helha.projetaemt_backend.application.folder.command.update.UpdateFolderInput;
import com.helha.projetaemt_backend.controllers.folder.exceptions.*;
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
                    description = "Business rule violation (parent folder does not belong to user)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found or parent folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict: folder title already exists for the same user",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping()
    public ResponseEntity<CreateFolderOutput> create(@Valid @RequestBody CreateFolderInput input){
        try{
            CreateFolderOutput output = folderCommandProcessor
                    .createFolerHandler
                    .handle(input);

            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(output.id)
                    .toUri();// obtient un objet URI
            return ResponseEntity
                    .created(location)
                    .body(output);
        }
        catch (IllegalArgumentException e){
            String msg = e.getMessage();
            if (msg.contains("User not found")) {
                throw new UserNotFoundException(input.userId);
            }
            if (msg.contains("Parent folder not found")) {
                int pfd = input.parentFolderId;
                throw new ParentFolderNotFoundException(pfd);
            }
            if (msg.contains("Parent folder does not belong")) {
                int pfd = input.parentFolderId;
                throw new ParentFolderDoesNotBelongToUserException(pfd, input.userId);
            }
            if (msg.contains("Folder already exists")) {
                throw new FolderAlreadyExistsException(input.userId, input.parentFolderId, input.title);
            }
            throw e;
        }
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
                    responseCode = "204",
                    description = "Folder successfully renamed"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input or business rule violation (empty title or folder does not belong to user)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
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
        try {
            folderCommandProcessor.updateFolderHandler.handle(input);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e) {
            String msg = e.getMessage() == null ? "" : e.getMessage();

            if (msg.contains("The folder title is mandatory")) {
                throw new FolderTitleMandatoryException();
            }
            if (msg.contains("Folder already exists")) {
                throw new FolderAlreadyExistsException(input.userId, null, input.title);
            }
            if (msg.contains("Folder does not belong")) {
                throw new ParentFolderDoesNotBelongToUserException(input.id, input.userId);
            }
            if (msg.contains("Folder not found")) {
                throw new FolderNotFoundException(input.id);
            }
            throw new FolderNotFoundException(input.id);
        }
    }

    @Operation(
            summary = "Supprimer un dossier",
            description = """
            Supprime un dossier par son identifiant.
            - Le dossier doit appartenir à l'utilisateur fourni (userId).
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
                    description = "Folder not found (does not exist or does not belong to this user)",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @DeleteMapping("/{folderId}")
    public ResponseEntity<Void> delete(@PathVariable int folderId){
        try {
            folderCommandProcessor.deleteFolderHandler.handle(folderId);
            return ResponseEntity.noContent().build();
        }
        catch (IllegalArgumentException e) {
            throw new FolderNotFoundException(folderId);
        }
    }
}
