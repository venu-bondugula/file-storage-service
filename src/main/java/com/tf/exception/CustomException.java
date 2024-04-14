package com.tf.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends RuntimeException {

    private final Throwable cause;
    private final HttpStatus errorCode;
    private HttpHeaders headers;

    public CustomException() {
        super("Upload failed");
        this.cause = null;
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public CustomException(String message) {
        super(message);
        this.cause = null;
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public CustomException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
        this.errorCode = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public CustomException(String message, HttpStatus errorCode) {
        super(message);
        this.cause = null;
        this.errorCode = errorCode;
    }

    public CustomException(String message, Throwable cause, HttpStatus errorCode) {
        super(message);
        this.cause = cause;
        this.errorCode = errorCode;
    }
}
