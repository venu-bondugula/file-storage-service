package com.tf.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<String> handleFileUploadException(CustomException e) {
        return new ResponseEntity<>(e.getMessage(), e.getErrorCode());
    }
}
