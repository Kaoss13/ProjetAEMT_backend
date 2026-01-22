package com.helha.projetaemt_backend.application.user.command.login;

import com.helha.projetaemt_backend.application.utils.ICommandHandler;
import com.helha.projetaemt_backend.infrastructure.user.DbUser;
import com.helha.projetaemt_backend.infrastructure.user.IUserRepository;
import com.helha.projetaemt_backend.infrastructure.utils.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LoginUserHandler implements ICommandHandler<LoginUserInput, LoginUserOutput> {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginUserHandler(IUserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public LoginUserOutput handle(LoginUserInput input) {
        DbUser dbUser = userRepository.findByUserName(input.userName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Identifiants invalides"
                ));

        if (!passwordEncoder.matches(input.password, dbUser.hashPassword)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Identifiants invalides"
            );
        }

        LoginUserOutput output = new LoginUserOutput();
        output.id = dbUser.id;
        output.userName = dbUser.userName;
        output.token = jwtService.generateToken(dbUser.id, dbUser.userName);

        return output;
    }
}
