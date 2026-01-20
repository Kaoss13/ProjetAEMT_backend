package com.helha.projetaemt_backend.application.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class InvalidCredentialsException extends ErrorResponseException {

    public InvalidCredentialsException() {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Nom d'utilisateur ou mot de passe incorrect"
                ),
                null
        );
    }
}
