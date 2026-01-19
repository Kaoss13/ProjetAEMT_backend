package com.helha.projetaemt_backend.application.user.command.create;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
        if (userRepository.existsByUserName(input.userName)) {
            throw new IllegalArgumentException("Un utilisateur avec ce nom existe déjà");
        }

        DbUser dbUser = new DbUser();
        dbUser.userName = input.userName;
        dbUser.hashPassword = passwordEncoder.encode(input.password);

        DbUser savedUser = userRepository.save(dbUser);

        CreateUserOutput output = new CreateUserOutput();
        output.id = savedUser.id;
        output.userName = savedUser.userName;

        return output;
    }
}
