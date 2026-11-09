package com.Kurzgesagtt.DigitalSignature.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> notFound(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Recurso nao encontrado."));
    }

    @ExceptionHandler({DateTimeParseException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<?> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("erro", "Requisicao invalida."));
    }

    @ExceptionHandler({DataIntegrityViolationException.class, org.hibernate.exception.ConstraintViolationException.class})
    public ResponseEntity<?> conflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "A operacao conflita com os registros existentes."));
    }
}
