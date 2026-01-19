package com.helha.projetaemt_backend.application.user.command.create;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

public class CreateUserHandler implements ICommandHandler<CreateUserInput, CreateUserOutput> {



    private final IUserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    public CreateUserHandler(IUserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }



    @Override
    public CreateUserOutput handle(CreateUserInput input) {
        return null;
    }
}
