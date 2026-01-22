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
            summary = "Get all folders and notes for a user",
            description = """
                Returns two flat lists:
                - folders
                - notes

                The frontend reconstructs the tree via buildFolderTree(folders, notes).
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Folders and notes retrieved",
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
            summary = "Export a folder to ZIP",
            description = """
            Generates a ZIP archive containing the folder,
            its subfolders and associated notes.
            The file is returned for download.
            """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "ZIP archive generated successfully",
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
                    description = "Error generating ZIP",
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
