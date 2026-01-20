package com.helha.projetaemt_backend.application.user.command.login;

import com.helha.projetaemt_backend.application.user.exceptions.InvalidCredentialsException;
import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class LoginUserHandler implements ICommandHandler<LoginUserInput, LoginUserOutput> {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginUserHandler(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginUserOutput handle(LoginUserInput input) {
        DbUser dbUser = userRepository.findByUserName(input.userName)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(input.password, dbUser.hashPassword)) {
            throw new InvalidCredentialsException();
        }

        LoginUserOutput output = new LoginUserOutput();
        output.id = dbUser.id;
        output.userName = dbUser.userName;

        return output;
    }
}
