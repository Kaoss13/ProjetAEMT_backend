package com.helha.projetaemt_backend.controllers.user;

import com.helha.projetaemt_backend.application.user.command.UserCommandProcessor;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserInput;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserOutput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserInput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserOutput;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserCommandController {

    private final UserCommandProcessor userCommandProcessor;

    public UserCommandController(UserCommandProcessor userCommandProcessor) {
        this.userCommandProcessor = userCommandProcessor;
    }

    @PostMapping
    public ResponseEntity<CreateUserOutput> createUser(@Valid @RequestBody CreateUserInput input) {
        CreateUserOutput output = userCommandProcessor.create(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(output);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserOutput> login(@Valid @RequestBody LoginUserInput input) {
        LoginUserOutput output = userCommandProcessor.login(input);
        return ResponseEntity.ok(output);
    }
}
