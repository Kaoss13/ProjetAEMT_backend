package com.helha.projetaemt_backend.controllers.user;

import com.helha.projetaemt_backend.application.user.command.UserCommandProcessor;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserInput;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserOutput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserInput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserOutput;
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
@RequestMapping("/users")
public class UserCommandController {

    private final UserCommandProcessor userCommandProcessor;

    public UserCommandController(UserCommandProcessor userCommandProcessor) {
        this.userCommandProcessor = userCommandProcessor;
    }

    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    headers = @Header(
                            name = "Location",
                            description = "Location of created resource"
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User already exists",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<CreateUserOutput> create(@Valid @RequestBody CreateUserInput input) {
        CreateUserOutput output = userCommandProcessor.createUserHandler.handle(input);
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
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = ProblemDetail.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginUserOutput> login(@Valid @RequestBody LoginUserInput input) {
        LoginUserOutput output = userCommandProcessor.loginUserHandler.handle(input);
        return ResponseEntity.ok(output);
    }
}
