package com.helha.projetaemt_backend.controllers.note;

import com.helha.projetaemt_backend.application.note.command.NoteCommandProcessor;
import com.helha.projetaemt_backend.application.note.command.create.CreateNoteInput;
import com.helha.projetaemt_backend.application.note.command.create.CreateNoteOutput;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteInput;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
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
@RequestMapping("/notes")
public class NoteCommandController {
    private final NoteCommandProcessor noteCommandProcessor;
    private INoteRepository noteRepository;

    public NoteCommandController(NoteCommandProcessor noteCommandProcessor, INoteRepository noteRepository) {
        this.noteCommandProcessor = noteCommandProcessor;
    }

    @Operation(
            summary = "Créer une note",
            description = """
        Crée une nouvelle note pour un utilisateur donné.

        Règles :
        - L'utilisateur doit exister.
        - La note est associée à un dossier existant.
        - Si idFolder = 0, la note est créée dans le dossier racine de l'utilisateur.
        - Le titre est optionnel.
        - Le contenu est optionnel.

        Remarques :
        - Les métadonnées (taille, nombre de lignes, mots, caractères) sont calculées automatiquement.
        - Les dates de création et de mise à jour sont générées côté serveur.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Note successfully created",
                    headers = @Header(
                            name = "Location",
                            description = "Location of created resource"
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User or folder not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })

    @PostMapping()
    public ResponseEntity<CreateNoteOutput> create(@Valid @RequestBody CreateNoteInput input) throws IllegalAccessException {
            CreateNoteOutput output = noteCommandProcessor.createNoteHandler.handle(input);
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
            summary = "Mettre à jour une note",
            description = """
            Met à jour une note existante.

            Règles :
            - La note doit exister.
            - title et content sont optionnels : seuls les champs fournis (non null) sont modifiés.

            Retour :
            - 204 si la mise à jour a réussi.
            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Note successfully updated"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PutMapping()
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateNoteInput input) {
        noteCommandProcessor.updateNoteHandler.handle(input);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Supprimer une note",
            description = """
        Supprime une note existante.

        Règles :
        - La note doit exister.

        Remarque :
        - La suppression est définitive.
        """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Note successfully deleted"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Note not found",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable() int id) {
        noteCommandProcessor.deleteNoteHandler.handle(id);
        return ResponseEntity.noContent().build();
    }
}
