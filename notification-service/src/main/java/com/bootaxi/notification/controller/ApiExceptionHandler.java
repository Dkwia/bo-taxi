package com.bootaxi.notification.controller;

import com.bootaxi.notification.exception.ConflictException;
import com.bootaxi.notification.exception.ForbiddenException;
import com.bootaxi.notification.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException exception) {
        return createProblem(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail handleNotFound(NotFoundException exception) {
        return createProblem(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail handleConflict(ConflictException exception) {
        return createProblem(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    ProblemDetail handleForbidden(ForbiddenException exception) {
        return createProblem(HttpStatus.FORBIDDEN, exception.getMessage());
    }

    private ProblemDetail createProblem(HttpStatus status, String message) {
        ProblemDetail detail = ProblemDetail.forStatus(status);
        detail.setDetail(message);
        return detail;
    }
}
