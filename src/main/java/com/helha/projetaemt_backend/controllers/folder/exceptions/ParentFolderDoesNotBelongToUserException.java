package com.helha.projetaemt_backend.controllers.folder.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class ParentFolderDoesNotBelongToUserException extends ErrorResponseException {
    public ParentFolderDoesNotBelongToUserException(int parentFolderId, int userId) {
        super(
                HttpStatus.BAD_REQUEST,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.BAD_REQUEST,
                        "Parent folder %d does not belong to user %d".formatted(parentFolderId, userId)
                ),
                null
        );
    }
}
