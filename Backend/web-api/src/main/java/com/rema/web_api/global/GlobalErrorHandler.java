package com.rema.web_api.global;

import com.rema.web_api.user.dto.ErrorDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Nieprawidłowe dane wejściowe!");

        ErrorDTO errorDTO = new ErrorDTO(errorMessage, HttpStatus.BAD_REQUEST.value());

        return ResponseEntity
                .badRequest()
                .body(errorDTO);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalState(IllegalStateException e) {

        ErrorDTO errorDTO = new ErrorDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value());

        return ResponseEntity
                .badRequest()
                .body(errorDTO);
    }
}
