package com.ford.vdcc.poc.batch.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@ControllerAdvice
public class ExceptionTranslator {
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String  processRuntimeException(RuntimeException e) {
        return createErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.", e);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String  processNotFoundException(RuntimeException e) {
        return createErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR, "An internal server error occurred.", e);
    }
    private String createErrorDTO(HttpStatus status, String message, Exception e) {
    return e.getMessage();
    }
}
