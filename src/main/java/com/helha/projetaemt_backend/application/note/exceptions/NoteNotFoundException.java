package com.helha.projetaemt_backend.application.note.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class NoteNotFoundException extends ErrorResponseException {
    public NoteNotFoundException(int id) {
        super(
                HttpStatus.NOT_FOUND,
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.NOT_FOUND,
                        "Note with %d is not found".formatted(id)
                ),
                null
        );
    }
}
