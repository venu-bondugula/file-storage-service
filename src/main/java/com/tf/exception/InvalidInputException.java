package com.tf.exception;

import org.springframework.http.HttpStatus;

public class InvalidInputException extends CustomException {
    public InvalidInputException(String errorUploadingFile, Throwable cause) {
        super(errorUploadingFile, cause, HttpStatus.BAD_REQUEST);
    }

    public InvalidInputException(String errorUploadingFile) {
        super(errorUploadingFile, HttpStatus.BAD_REQUEST);
    }
}
