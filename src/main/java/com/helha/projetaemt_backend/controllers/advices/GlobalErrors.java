package com.helha.projetaemt_backend.controllers.advices;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalErrors {

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail notFound(ResponseStatusException exception){
        ProblemDetail problemDetail = ProblemDetail.forStatus(exception.getStatusCode());
        problemDetail.setTitle(exception.getStatusCode().toString());
        problemDetail.setDetail(exception.getReason());

        return problemDetail;
    }
}
