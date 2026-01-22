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
            summary = "Create a note",
            description = """
        Creates a new note for a given user.

        Rules:
        - The user must exist.
        - The note is associated with an existing folder.
        - If idFolder = 0, the note is created in the user's root folder.
        - Title is optional.
        - Content is optional.

        Notes:
        - Metadata (size, line count, word count, character count) are computed automatically.
        - Creation and update dates are generated server-side.
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
            summary = "Update a note",
            description = """
            Updates an existing note.

            Rules:
            - The note must exist.
            - Title and content are optional: only provided (non-null) fields are modified.

            Returns:
            - 204 if update was successful.
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
            summary = "Delete a note",
            description = """
        Deletes an existing note.

        Rules:
        - The note must exist.

        Note:
        - Deletion is permanent.
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
