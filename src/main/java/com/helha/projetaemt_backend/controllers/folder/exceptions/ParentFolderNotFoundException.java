package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class ParentFolderNotFoundException extends ErrorResponseException {
    public ParentFolderNotFoundException(int parentFolderId) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Parent folder with id %d was not found".formatted(parentFolderId)
                ),
                null
        );
    }
}
