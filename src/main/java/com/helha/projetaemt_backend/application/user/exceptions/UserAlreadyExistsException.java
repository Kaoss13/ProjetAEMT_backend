package com.helha.projetaemt_backend.application.user.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UserAlreadyExistsException extends ErrorResponseException {

    public UserAlreadyExistsException(String userName) {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Un utilisateur avec le nom '%s' existe déjà".formatted(userName)
                ),
                null
        );
    }
}
