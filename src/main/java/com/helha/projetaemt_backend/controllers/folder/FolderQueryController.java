package com.helha.projetaemt_backend.controllers.folder;

import com.helha.projetaemt_backend.application.folder.query.FolderQueryProcessor;
import com.helha.projetaemt_backend.application.folder.query.getAll.GetAllFoldersWithNotesOutput;
import com.helha.projetaemt_backend.controllers.folder.exceptions.UserNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    public FolderQueryController(FolderQueryProcessor folderQueryProcessor){
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
        try{
            GetAllFoldersWithNotesOutput output = folderQueryProcessor
                    .getAllFoldersWithNotesHandler
                    .handle(userId);

            return ResponseEntity.ok(output);
        }
        catch (IllegalArgumentException e){
            String msg = e.getMessage() == null ? "" : e.getMessage();

            if (msg.contains("The user with this ID does not exist")) {
                throw new UserNotFoundException(userId);
            }
            throw e;
        }
    }
}
