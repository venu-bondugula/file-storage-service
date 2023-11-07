package com.tf.exception;

import org.springframework.http.HttpStatus;

public class EmptyFileException extends CustomException {
    public EmptyFileException() {
        super("The provided file is empty");
        setErrorCode(HttpStatus.BAD_REQUEST); // Set the HTTP status code to BAD_REQUEST
    }
}
