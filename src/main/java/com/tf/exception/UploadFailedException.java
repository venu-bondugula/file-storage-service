package com.tf.exception;

import org.springframework.http.HttpStatus;

public class UploadFailedException extends CustomException {
    public UploadFailedException(String message) {
        super(message);
        this.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public UploadFailedException() {
        this("File upload failed");
    }
}
