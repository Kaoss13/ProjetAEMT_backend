package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class FolderNotFoundException extends ErrorResponseException {
    public FolderNotFoundException(int folderId) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Folder with id %d was not found".formatted(folderId)
                ),
                null
        );
    }
}
