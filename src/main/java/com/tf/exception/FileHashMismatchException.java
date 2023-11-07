package com.tf.exception;

import org.springframework.http.HttpStatus;

public class FileHashMismatchException extends CustomException {
    public FileHashMismatchException() {
        super("File hash mismatch");
        this.setErrorCode(HttpStatus.BAD_REQUEST);
    }
}
