package com.helha.projetaemt_backend.controllers.note;

import com.helha.projetaemt_backend.application.note.command.NoteCommandProcessor;
import com.helha.projetaemt_backend.application.note.command.create.CreateNoteInput;
import com.helha.projetaemt_backend.application.note.command.create.CreateNoteOutput;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteHandler;
import com.helha.projetaemt_backend.application.note.command.update.UpdateNoteInput;
import com.helha.projetaemt_backend.infrastructure.note.INoteRepository;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
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

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    headers = @Header(
                            name = "Location",
                            description = "Location of created resource")),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
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

    @ApiResponses({
            @ApiResponse(responseCode = "200"),
            @ApiResponse(responseCode = "400",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            ),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @PutMapping()
    public ResponseEntity<Void> update(@Valid @RequestBody UpdateNoteInput input) {
        try {
            noteCommandProcessor.updateNoteHandler.handle(input);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", content = @Content),
            @ApiResponse(responseCode = "404",
                    content = @Content(schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable() int id) {
        try {
            noteCommandProcessor.deleteNoteHandler.handle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException();
        }
    }
}
