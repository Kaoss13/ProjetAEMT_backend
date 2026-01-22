package com.helha.projetaemt_backend.application.user.command.create;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.dossier.DbFolder;
import com.helha.projetaemt_backend.infrastructure.dossier.IFolderRepository;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CreateUserHandler implements ICommandHandler<CreateUserInput, CreateUserOutput> {

    private final IUserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final IFolderRepository folderRepository;

    public CreateUserHandler(IUserRepository userRepository, ModelMapper modelMapper, PasswordEncoder passwordEncoder, IFolderRepository folderRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.folderRepository = folderRepository;
    }

    @Override
    public CreateUserOutput handle(CreateUserInput input) {
        if (userRepository.existsByUserName(input.userName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User already exists"
            );
        }

        DbUser dbUser = new DbUser();
        dbUser.userName = input.userName;
        dbUser.hashPassword = passwordEncoder.encode(input.password);

        DbUser savedUser = userRepository.save(dbUser);


        DbFolder rootFolder = new DbFolder();
        rootFolder.setUser(savedUser);
        rootFolder.setTitle("Racine");
        rootFolder.setCreatedAt(LocalDateTime.now());
        rootFolder.setParentFolder(null);
        folderRepository.save(rootFolder);


        /*CreateUserOutput output = new CreateUserOutput();
        output.id = savedUser.id;
        output.userName = savedUser.userName;

        return output;*/
        return modelMapper.map(savedUser, CreateUserOutput.class);
    }
}
