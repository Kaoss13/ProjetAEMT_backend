package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UserNotFoundException extends ErrorResponseException {
    public UserNotFoundException(int userId) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "User with id %d was not found".formatted(userId)
                ),
                null
        );
    }
}
