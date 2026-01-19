package com.helha.projetaemt_backend.application.user.command;

import com.helha.projetaemt_backend.application.user.command.create.CreateUserHandler;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserInput;
import com.helha.projetaemt_backend.application.user.command.create.CreateUserOutput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserHandler;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserInput;
import com.helha.projetaemt_backend.application.user.command.login.LoginUserOutput;
import org.springframework.stereotype.Service;

@Service
public class UserCommandProcessor {

    public final CreateUserHandler createUserHandler;
    public final LoginUserHandler loginUserHandler;

    public UserCommandProcessor(CreateUserHandler createUserHandler, LoginUserHandler loginUserHandler) {
        this.createUserHandler = createUserHandler;
        this.loginUserHandler = loginUserHandler;
    }





}
