package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class FolderTitleMandatoryException extends ErrorResponseException {
    public FolderTitleMandatoryException() {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "The folder title is mandatory."
                ),
                null
        );
    }
}
