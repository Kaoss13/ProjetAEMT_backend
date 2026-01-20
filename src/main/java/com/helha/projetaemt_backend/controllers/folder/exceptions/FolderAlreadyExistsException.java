package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class FolderAlreadyExistsException extends ErrorResponseException {
    public FolderAlreadyExistsException(int userId, Integer parentFolderId, String title) {
        super(
                HttpStatus.CONFLICT,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.CONFLICT,
                        "Folder '%s' already exists for user %d in this location".formatted(title, userId)
                ),
                null
        );
    }
}
