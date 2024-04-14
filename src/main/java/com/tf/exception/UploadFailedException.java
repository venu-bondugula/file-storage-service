package com.tf.exception;

import org.springframework.http.HttpStatus;

public class UploadFailedException extends CustomException {
    public UploadFailedException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public UploadFailedException(String message, HttpStatus errorCode) {
        super(message, errorCode);
    }

    public UploadFailedException(String errorUploadingFile, Throwable cause) {
        super(errorUploadingFile, cause, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
